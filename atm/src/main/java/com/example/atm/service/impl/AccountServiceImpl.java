package com.example.atm.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.example.atm.Utils.JwtUtils;
import com.example.atm.Utils.PasswordUtils;
import com.example.atm.common.exception.LoginFailedException;
import com.example.atm.common.exception.*;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.LoginResponse;
import com.example.atm.dto.RegisterRequest;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.mapper.TransactionMapper;
import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.atm.Utils.RedisConstants.FAIL_KEY;
import static com.example.atm.Utils.RedisConstants.LOCK_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
	private final JwtUtils jwtUtils;
	private final AccountMapper accountMapper;
	private final TransactionMapper transactionMapper;
	private final TransactionService transactionService;
	private final StringRedisTemplate stringRedisTemplate;
	@Value("${atm.aes-key}")
	private String aesKey;
	//  BCrypt 实例
	private static final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

	/**
	 * 生成以 6222 开头的 16 位卡号字符串，剩余位数由随机数字组成。
	 *
	 * @return 生成的 16 位卡号字符串
	 */
	private String generateCard() {
		StringBuilder sb = new StringBuilder("6222");
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		while (sb.length() < 16) sb.append(rnd.nextInt(0, 10));
		return sb.toString();
	}

	//  注册：AES 解密 → BCrypt 加密 → 存库。卡号、余额、状态都由服务端决定
	@Override
	public Account register(RegisterRequest req) {
		String decrypted = decryptUserPassword(req.getPassword());
		// 格式校验放在解密之外，避免被解密的 catch 吞掉
		if (!decrypted.matches("\\d{6}")) {
			throw new DataFormatException("密码格式异常");
		}
		// 用户基本信息
		Account account = new Account();
		account.setName(req.getName().trim());
		account.setSex(req.getSex());
		account.setDailyLimit(req.getDailyLimit());
		account.setBalance(BigDecimal.ZERO);
		// 初始化最大限额为当前限额+1000 最小限额为当前限额
		account.setMaxLimit(req.getDailyLimit().add(BigDecimal.valueOf(1000)));
		account.setMinLimit(req.getDailyLimit());
		//  存 BCrypt hash，而不是明文
		account.setPassword(bcrypt.encode(decrypted));

		String card;
		do {
			card = generateCard();
		} while (accountMapper.findByCard(card) != null);
		account.setCard(card);

		accountMapper.insert(account);

		Account saved = accountMapper.findByCard(card);
		saved.setPassword(null); //  注册成功不返回 hash
		return saved;
	}

	//  登录：AES 解密 → BCrypt 比对 → 返回 LoginResponse（无密码）
	@Override
	public LoginResponse login(String card, String password) {
		checkLocked(card);
		Account account = accountMapper.login(card);
		//注销时间超过7天或查不到账户统一记录卡号不存在并返回通用错误提示
		if (account == null) {
			loginFail(card, null);
			throw new LoginFailedException("卡号或密码错误");
		}
		boolean cancelled = "CANCEL".equals(account.getStatus());
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
		if (cancelled && account.getCloseTime() != null
				&& now.isAfter(account.getCloseTime().plusDays(7))) {
			loginFail(card, account.getStatus());
			throw new LoginFailedException("卡号或密码错误");
		}
		String decryptedPassword = decryptUserPassword(password);
		//  BCrypt 比对，matches() 内部处理盐，无需手动提取
		if (!bcrypt.matches(decryptedPassword, account.getPassword())) {
			loginFail(card, account.getStatus());
			throw new LoginFailedException("卡号或密码错误");
		}
		if (cancelled) {
			// 注销未满7天的处理
			log.info("未满7天，取消注销");
			accountMapper.restoreCancelAccount(account.getCard());
		}
		Map<String, Object> claims = new HashMap<>();
		claims.put("card", account.getCard());
		claims.put("name", account.getName());
		String token;
		if (card.matches("[a-z]+") && account.getRole().equals("ADMIN")) {
			// 特性卡号使用特殊的jwt密钥
			token = jwtUtils.getAdminToken(claims);
		} else {
			token = jwtUtils.generateToken(claims);
		}
		stringRedisTemplate.delete(FAIL_KEY + card);
		//  返回 DTO，不含密码字段等敏感字段
		LoginResponse rep = BeanUtil.copyProperties(account, LoginResponse.class);
		rep.setToken(token);
		return rep;
	}

	private @NonNull String decryptUserPassword(String password) {
		String decryptedPassword;
		try {
			decryptedPassword = PasswordUtils.decryptPassword(password, aesKey);
		} catch (IllegalArgumentException iae) {
			log.error("加密配置错误: {}", iae.getMessage());
			throw new CryptoServiceException("服务器错误");
		} catch (Exception e) {
			log.error("密码解密失败: {}", e.getMessage());
			throw new CryptoServiceException("服务器错误");
		}
		return decryptedPassword;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Account deposit(DepositRequest req) {
		checkPositive(req.getAmount());
		Account a = accountMapper.findByCardForUpdate(req.getCard());
		if (a == null || "CANCEL".equals(a.getStatus())) throw new AccountNotFoundException("账户不存在");

		BigDecimal newBalance = a.getBalance().add(req.getAmount());
		accountMapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "DEPOSIT", req.getAmount(), "存款");

		a.setBalance(newBalance);
		return a;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Account withdraw(WithdrawRequest req) {
		checkPositive(req.getAmount());
		Account a = accountMapper.findByCardForUpdate(req.getCard());
		if (a == null || "CANCEL".equals(a.getStatus())) throw new AccountNotFoundException("账户不存在");
		if (a.getBalance().compareTo(req.getAmount()) < 0) throw new InsufficientBalanceException("余额不足");
		checkDailyLimit(a, req.getAmount());
		BigDecimal newBalance = a.getBalance().subtract(req.getAmount());
		accountMapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "WITHDRAW", req.getAmount(), "取款");

		a.setBalance(newBalance);
		return a;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Account transfer(TransferRequest req) {
		checkPositive(req.getAmount());
		String c1 = req.getFromCard(), c2 = req.getToCard();
		boolean fromFirst = c1.compareTo(c2) <= 0;
		Account first = accountMapper.findByCardForUpdate(fromFirst ? c1 : c2);
		Account second = accountMapper.findByCardForUpdate(fromFirst ? c2 : c1);
		Account from = fromFirst ? first : second;
		Account to = fromFirst ? second : first;
		if (from == null || to == null || "CANCEL".equals(to.getStatus()))
			throw new AccountNotFoundException("账户不存在");
		if (from.getCard().equals(to.getCard())) throw new DataFormatException("不能转账给自己");
		if (from.getBalance().compareTo(req.getAmount()) < 0) throw new InsufficientBalanceException("余额不足");
		checkDailyLimit(from, req.getAmount());
		BigDecimal fromNew = from.getBalance().subtract(req.getAmount());
		BigDecimal toNew = to.getBalance().add(req.getAmount());
		accountMapper.updateBalance(from.getCard(), fromNew);
		accountMapper.updateBalance(to.getCard(), toNew);
		transactionService.recordTransfer(
				req.getFromCard(),
				req.getToCard(),
				req.getAmount(),
				from.getName(),
				to.getName()
		);
		from.setBalance(fromNew);
		return from;
	}

	//  修改密码：AES 解密 → BCrypt 比对旧密码 → BCrypt 加密新密码存库
	@Override
	public boolean changePassword(String card, String oldPwd, String newPwd) {
		Account a = accountMapper.findByCard(card);
		if (a == null) throw new AccountNotFoundException("用户不存在");
		assertNotDemoAccount(a);
		String decryptedOld;
		String decryptedNew;
		try {
			decryptedOld = PasswordUtils.decryptPassword(oldPwd, aesKey);
			decryptedNew = PasswordUtils.decryptPassword(newPwd, aesKey);
		} catch (Exception e) {
			log.error("修改密码时解密失败: {}", e.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		}

		// 校验的是解密后的明文：新密码必须是 6 位纯数字
		if (!decryptedNew.matches("\\d{6}")) {
			throw new DataFormatException("新密码必须为6位数字");
		}

		// 旧密码只需要和库里的 BCrypt 哈希比对，不用再校验格式
		if (!bcrypt.matches(decryptedOld, a.getPassword())) {
			throw new PasswordComparisonException("旧密码错误");
		}

		accountMapper.updatePassword(card, bcrypt.encode(decryptedNew));
		return true;
	}

	/**
	 * 根据 card 查询账户信息。
	 *
	 * @param card 账户卡号
	 * @return 对应的 Account 信息；如果未找到则返回 null
	 */
	@Override
	public Account getInfo(String card) {
		return accountMapper.findByCard(card);
	}

	/**
	 * <p>注销账户</p>
	 * <p>只有当账户是非测试账号且余额为零时，才能注销该账户。</p>
	 * <p>如果账户余额不为零，则会抛出 {@code BalanceNotZeroException} 异常，
	 * 且不会注销该账户。</p>
	 * <p>如果账户状态为 TEST，则会抛出 {@code AccountVerificationException} 异常，
	 * 且不会注销该账户。</p>
	 *
	 * @param card     账户卡号
	 * @param password 账户密码
	 * @throws BalanceNotZeroException      如果账户余额不为零
	 * @throws AccountVerificationException 如果账户状态为 TEST
	 */
	@Override
	public void closeAccount(String card, String password) {
		Account user = accountMapper.findByCard(card);
		assertNotDemoAccount(user);
		BigDecimal balance = user.getBalance();
		String decryptedPassword = decryptUserPassword(password);
		if (!bcrypt.matches(decryptedPassword, user.getPassword())) {
			throw new PasswordComparisonException("密码错误");
		}
		if (balance.compareTo(BigDecimal.ZERO) > 0) {
			throw new BalanceNotZeroException("账户还有余额,注销失败");
		}
		int rows = accountMapper.closeAccount(card, LocalDateTime.now(ZoneId.of("Asia/Shanghai")), balance);
		if (rows == 0) {
			// 余额在读取之后发生了变动（或状态已变），统一终止
			throw new ConcurrentUpdateException("账户状态已变化，请重试");
		}
	}

	@Override
	public void changeLimit(String card,BigDecimal limit) {
		Account account = accountMapper.findByCard(card);
		BigDecimal max = account.getMaxLimit();
		BigDecimal min = account.getMinLimit();
		if(limit.compareTo(min)<0||limit.compareTo(max)>0){
			throw new DataFormatException("新的限额不在限额范围内！");
		}
		accountMapper.changeLimit(card,limit);
	}

	/**
	 * 校验金额是否为正数。
	 *
	 * @param amount 待校验的金额
	 * @throws DataFormatException 当 amount 小于或等于 BigDecimal.ZERO 时抛出
	 */
	private void checkPositive(BigDecimal amount) {
		if (!(amount.compareTo(BigDecimal.ZERO) > 0)) {
			throw new DataFormatException("金额不能为负数！");
		}
	}

	/**
	 * 根据卡号获取今日已支出
	 *
	 * @param card 卡号
	 * @return 今日支出金额
	 */
	private BigDecimal getTodayOutflow(String card) {
		return transactionMapper.sumTodayOutflow(card);
	}

	/**
	 * 判断本次支出是否超过限额
	 *
	 * @param account 用户信息
	 * @param amount  需要支出的金额
	 */
	private void checkDailyLimit(Account account, BigDecimal amount) {
		BigDecimal used = getTodayOutflow(account.getCard());
		if (used.add(amount).compareTo(account.getDailyLimit()) > 0) {
			throw new DailyLimitExceededException(
					"超过每日限额：限额 " + account.getDailyLimit() + " 元，今日已支出 " + used + " 元");
		}
	}

	/**
	 * 对失败的卡号进行失败计数，并在失败次数达到一定程度后进行阶段锁定
	 *
	 * @param card   卡号
	 * @param status 卡状态
	 */
	private void loginFail(String card, String status) {
		// 测试卡不计数
		if (status != null && status.equals("TEST")) {
			return;
		}
		String failKey = FAIL_KEY + card;
		// 使用原子操作递增计数（线程安全）
		Long failCount = stringRedisTemplate.opsForValue().increment(failKey, 1);
		// 首次失败时设置过期时间（24小时后自动重置）
		if (failCount != null && failCount == 1) {
			stringRedisTemplate.expire(failKey, Duration.ofDays(1));
		}
		// 判断是否需要锁定账户
		if (failCount != null && failCount >= 5) {
			lockAccount(card, failCount);
		}
	}

	/**
	 * 根据失败次数进行阶段锁定
	 *
	 * @param card      卡号
	 * @param failCount 失败次数
	 */
	private void lockAccount(String card, Long failCount) {
		String lockKey = LOCK_KEY + card;
		if (failCount == 5) {
			stringRedisTemplate.opsForValue().set(lockKey, "locked", Duration.ofMinutes(15));
			throw new AccountLockedException("账户已锁定，请15分钟后重试");
		} else if (failCount == 10) {
			stringRedisTemplate.opsForValue().set(lockKey, "locked", Duration.ofMinutes(30));
			throw new AccountLockedException("账户已锁定，请30分钟后重试");
		} else if (failCount == 15) {
			stringRedisTemplate.opsForValue().set(lockKey, "locked", Duration.ofHours(1));
			throw new AccountLockedException("账户已锁定，请1小时后重试");
		} else if (failCount == 20) {
			accountMapper.lockAccount(card);
			throw new AccountLockedException("账户已锁定，请联系管理员进行解锁");
		}
	}

	/**
	 * 登陆时调用，检查账户是否被锁定
	 * 被锁定后，根据不同情况进行返回
	 *
	 * <p>需要手动解锁：提示联系管理员</p>
	 * <p>临时锁定：提示尝试次数过多，需要等待一段时间才能再次尝试</p>
	 *
	 * @param card 要检查锁定状态的卡号
	 */
	private void checkLocked(String card) {
		Account account = accountMapper.findByCard(card);
		if (account == null) return;
		if ("LOCKED".equals(account.getStatus())) {
			throw new AccountLockedException("账户已锁定,请联系管理员进行解锁");
		}
		// getExpire 返回：>0 剩余秒数；-2 key 不存在；-1 存在但无过期时间
		Long sec = stringRedisTemplate.getExpire(LOCK_KEY + card);
		if (sec != null && sec > 0) {
			throw new AccountLockedException("尝试次数过多，请 " + formatWait(sec) + " 后再试");
		}
	}

	/**
	 * 格式化等待时间
	 *
	 * @param seconds 剩余秒数
	 * @return 格式化后的等待时间
	 */
	private String formatWait(long seconds) {
		long minutes = (seconds + 59) / 60;          // 向上取整
		return minutes >= 60 ? (minutes / 60) + " 小时" : minutes + " 分钟";
	}

	/**
	 * 定时删除已注销的账户数据。
	 * 每周四的4点删除注销15天及以上的用户
	 */
	@Scheduled(cron = "0 0 4 ? * thu", zone = "Asia/Shanghai")
	public void deleteCancelAccount() {
		LocalDateTime threshold = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).minusDays(15);
		int delCount = accountMapper.deleteCancelAccount(threshold);
		log.info("北京时间{},已注销账户数据已删除，共删除 {} 条", LocalDateTime.now(), delCount);
	}

	/**
	 * 校验指定 Account 是否不是测试账户。
	 *
	 * @param account 待校验的账户
	 * @throws AccountVerificationException 当账户状态为 TEST 时抛出
	 */
	private void assertNotDemoAccount(Account account) {
		if ("TEST".equals(account.getStatus())) {
			throw new AccountVerificationException("账户为测试账户，不能进行此操作");
		}
	}
}
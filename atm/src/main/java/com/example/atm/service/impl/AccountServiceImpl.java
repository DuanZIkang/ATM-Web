package com.example.atm.service.impl;

import com.example.atm.Utils.JwtUtils;
import com.example.atm.Utils.PasswordUtils;
import com.example.atm.common.exception.*;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.LoginResponse;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.mapper.TransactionMapper;
import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountMapper AccountMapper;
	private final TransactionMapper TransactionMapper;
	private final TransactionService transactionService;

	@Value("${atm.aes-key}")
	private String aesKey;
	//  BCrypt 实例
	private static final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

	private String generateCard() {
		StringBuilder sb = new StringBuilder("6222");
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		while (sb.length() < 16) sb.append(rnd.nextInt(0, 10));
		return sb.toString();
	}

	//  注册：AES 解密 → BCrypt 加密 → 存库
	@Override
	public Account register(Account account) {
		String decrypted;
		try {
			decrypted = PasswordUtils.decryptPassword(account.getPassword(), aesKey);
			if (!decrypted.matches("\\d{6}")) {
				throw new DataFormatException("密码格式异常");
			}
		} catch (Exception e) {
			log.error("注册时密码解密失败: {}", e.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		}
		//  存 BCrypt hash，而不是明文
		account.setPassword(bcrypt.encode(decrypted));

		if (account.getCard() == null || account.getCard().isEmpty()) {
			String card;
			do {
				card = generateCard();
			}
			while (AccountMapper.findByCard(card) != null);
			account.setCard(card);
		}

		AccountMapper.insert(account);

		Account saved = AccountMapper.findByCard(account.getCard());
		saved.setPassword(null); //  注册成功不返回 hash
		return saved;
	}

	//  登录：AES 解密 → BCrypt 比对 → 返回 LoginResponse（无密码）
	@Override
	public LoginResponse login(String card, String password) {
		Account account = AccountMapper.login(card);
		if (account == null) throw new AccountNotFoundException("卡号不存在");
		log.info("aesKey长度: {}, 值: '{}'", aesKey.getBytes(StandardCharsets.UTF_8).length, aesKey);
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

		//  BCrypt 比对，matches() 内部处理盐，无需手动提取
		if (!bcrypt.matches(decryptedPassword, account.getPassword())) {
			throw new PasswordComparisonException("密码错误");
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("card", account.getCard());
		claims.put("name", account.getName());
		String token = JwtUtils.generateToken(claims);

		//  返回 DTO，不含密码字段
		return new LoginResponse(
				account.getCard(),
				account.getName(),
				account.getSex(),
				account.getDailyLimit(),
				account.getBalance(),
				token
		);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Account deposit(DepositRequest req) {
		checkPositive(req.getAmount());
		Account a = AccountMapper.findByCard(req.getCard());
		if (a == null) throw new AccountNotFoundException("账户不存在");

		BigDecimal newBalance = a.getBalance().add(req.getAmount());
		AccountMapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "DEPOSIT", req.getAmount(), "存款");

		a.setBalance(newBalance);
		return a;
	}


	@Override
	public Account withdraw(WithdrawRequest req) {
		checkPositive(req.getAmount());
		Account a = AccountMapper.findByCard(req.getCard());
		if (a == null) throw new AccountNotFoundException("账户不存在");
		if (a.getBalance().compareTo(req.getAmount()) < 0) throw new InsufficientBalanceException("余额不足");
		checkDailyLimit(a, req.getAmount());
		BigDecimal newBalance = a.getBalance().subtract(req.getAmount());
		AccountMapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "WITHDRAW", req.getAmount(), "取款");

		a.setBalance(newBalance);
		return a;
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Account transfer(TransferRequest req) {
		checkPositive(req.getAmount());
		Account from = AccountMapper.findByCard(req.getFromCard());
		Account to = AccountMapper.findByCard(req.getToCard());
		if (from == null || to == null) throw new AccountNotFoundException("账户不存在");
		if (!(from.getBalance().compareTo(req.getAmount()) > 0)) throw new InsufficientBalanceException("余额不足");
		checkDailyLimit(from, req.getAmount());
		BigDecimal fromNew = from.getBalance().subtract(req.getAmount());
		BigDecimal toNew = to.getBalance().add(req.getAmount());
		AccountMapper.updateBalance(req.getFromCard(), fromNew);
		AccountMapper.updateBalance(req.getToCard(), toNew);

		from.setBalance(fromNew);
		return from;
	}

	//  修改密码：AES 解密 → BCrypt 比对旧密码 → BCrypt 加密新密码存库
	@Override
	public boolean changePassword(String card, String oldPwd, String newPwd) {
		Account a = AccountMapper.findByCard(card);
		if (a == null) throw new AccountNotFoundException("用户不存在");

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

		AccountMapper.updatePassword(card, bcrypt.encode(decryptedNew));
		return true;
	}

	@Override
	public Account getInfo(String card) {
		return AccountMapper.findByCard(card);
	}

	private void checkPositive(BigDecimal amount) {
		if (!(amount.compareTo(BigDecimal.ZERO) > 0)) {
			throw new DataFormatException("金额不能为负数！");
		}
	}

	/**
	 * 根据卡号获取今日已支出
	 * @param card 卡号
	 * @return 今日支出金额
	 */
	private BigDecimal getTodayOutflow(String card) {
		return TransactionMapper.sumTodayOutflow(card);
	}

	/**
	 * 判断本次支出是否超过限额
	 * @param account 用户信息
	 * @param amount 需要支出的金额
	 */
	private void checkDailyLimit(Account account, BigDecimal amount) {
		BigDecimal used = getTodayOutflow(account.getCard());
		if (used.add(amount).compareTo(account.getDailyLimit()) > 0) {
			throw new DailyLimitExceededException(
					"超过每日限额：限额 " + account.getDailyLimit() + " 元，今日已支出 " + used + " 元");
		}
	}
}
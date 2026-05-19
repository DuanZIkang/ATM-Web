package com.example.atm.service.impl;

import com.example.atm.Utils.JwtUtils;
import com.example.atm.Utils.PasswordUtils;
import com.example.atm.common.exception.CryptoServiceException;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.LoginResponse;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountMapper mapper;
	private final TransactionService transactionService;

	@Value("${atm.aes-key}")
	private String aesKey;
	// ✅ BCrypt 实例，strength=12 比默认 10 更安全，对 ATM 场景合适
	private static final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(12);

	private String generateCard() {
		StringBuilder sb = new StringBuilder("6222");
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		while (sb.length() < 16) sb.append(rnd.nextInt(0, 10));
		return sb.toString();
	}

	// ✅ 注册：AES 解密 → BCrypt 加密 → 存库
	@Override
	public Account register(Account account) {
		String decrypted;
		try {
			decrypted = PasswordUtils.decryptPassword(account.getPassword(),aesKey);
		} catch (Exception e) {
			log.error("注册时密码解密失败: {}", e.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		}
		// ✅ 存 BCrypt hash，而不是明文
		account.setPassword(bcrypt.encode(decrypted));

		if (account.getCard() == null || account.getCard().isEmpty()) {
			String card;
			do { card = generateCard(); }
			while (mapper.findByCard(card) != null);
			account.setCard(card);
		}

		mapper.insert(account);

		Account saved = mapper.findByCard(account.getCard());
		saved.setPassword(null); // ✅ 注册成功不返回 hash
		return saved;
	}

	// ✅ 登录：AES 解密 → BCrypt 比对 → 返回 LoginResponse（无密码）
	@Override
	public LoginResponse login(String card, String password) {
		Account account = mapper.login(card);
		if (account == null) throw new RuntimeException("卡号不存在");
		log.info("aesKey长度: {}, 值: '{}'", aesKey.getBytes(StandardCharsets.UTF_8).length, aesKey);
		String decryptedPassword;
		try {
			decryptedPassword = PasswordUtils.decryptPassword(password,aesKey);

		} catch (IllegalArgumentException iae) {
			log.error("加密配置错误: {}", iae.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		} catch (Exception e) {
			log.error("密码解密失败: {}", e.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		}

		// ✅ BCrypt 比对，matches() 内部处理盐，无需手动提取
		if (!bcrypt.matches(decryptedPassword, account.getPassword())) {
			throw new RuntimeException("密码错误");
		}

		Map<String, Object> claims = new HashMap<>();
		claims.put("card", account.getCard());
		claims.put("name", account.getName());
		String token = JwtUtils.generateToken(claims);

		// ✅ 返回 DTO，不含密码字段
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
	public Account deposit(DepositRequest req) {
		Account a = mapper.findByCard(req.getCard());
		if (a == null) throw new RuntimeException("账户不存在");

		double newBalance = a.getBalance() + req.getAmount();
		mapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "DEPOSIT", req.getAmount(), "存款");

		a.setBalance(newBalance);
		return a;
	}

	@Override
	public Account withdraw(WithdrawRequest req) {
		Account a = mapper.findByCard(req.getCard());
		if (a == null) throw new RuntimeException("账户不存在");
		if (a.getBalance() < req.getAmount()) throw new RuntimeException("余额不足");

		double newBalance = a.getBalance() - req.getAmount();
		mapper.updateBalance(req.getCard(), newBalance);
		transactionService.record(req.getCard(), "WITHDRAW", req.getAmount(), "取款");

		a.setBalance(newBalance);
		return a;
	}

	@Override
	public Account transfer(TransferRequest req) {
		Account from = mapper.findByCard(req.getFromCard());
		Account to = mapper.findByCard(req.getToCard());
		if (from == null || to == null) throw new RuntimeException("账户不存在");
		if (from.getBalance() < req.getAmount()) throw new RuntimeException("余额不足");

		double fromNew = from.getBalance() - req.getAmount();
		double toNew = to.getBalance() + req.getAmount();
		mapper.updateBalance(req.getFromCard(), fromNew);
		mapper.updateBalance(req.getToCard(), toNew);

		from.setBalance(fromNew);
		return from;
	}

	// ✅ 修改密码：AES 解密 → BCrypt 比对旧密码 → BCrypt 加密新密码存库
	@Override
	public boolean changePassword(String card, String oldPwd, String newPwd) {
		Account a = mapper.findByCard(card);
		if (a == null) throw new RuntimeException("用户不存在");

		String decryptedOld;
		String decryptedNew;
		try {
			decryptedOld = PasswordUtils.decryptPassword(oldPwd,aesKey);
			decryptedNew = PasswordUtils.decryptPassword(newPwd,aesKey);
		} catch (Exception e) {
			throw new CryptoServiceException("服务器内部错误");
		}

		// ✅ BCrypt 比对旧密码
		if (!bcrypt.matches(decryptedOld, a.getPassword())) {
			throw new RuntimeException("旧密码错误");
		}

		// ✅ 新密码 BCrypt 加密后存库
		mapper.updatePassword(card, bcrypt.encode(decryptedNew));
		return true;
	}

	@Override
	public Account getInfo(String card) {
		return mapper.findByCard(card);
	}
//	@jakarta.annotation.PostConstruct
//	public void migratePasswords() {
//		List<Account> all = mapper.findAll(); // mapper 里需要加 findAll 方法
//		for (Account a : all) {
//			if (a.getPassword() != null && !a.getPassword().startsWith("$2a$")) {
//				log.info("迁移账户 {} 的密码", a.getCard());
//				mapper.updatePassword(a.getCard(), bcrypt.encode(a.getPassword()));
//			}
//		}
//		log.info("密码迁移完成");
//	}
}
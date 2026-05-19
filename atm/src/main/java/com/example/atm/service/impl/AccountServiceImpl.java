package com.example.atm.service.impl;

import com.example.atm.Utils.JwtUtils;
import com.example.atm.Utils.PasswordUtils;
import com.example.atm.common.exception.CryptoServiceException;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

	private final AccountMapper mapper;
	private final TransactionService transactionService;

	// ✅ 自动生成 16 位银行卡号
	private String generateCard() {
		StringBuilder sb = new StringBuilder("6222");
		ThreadLocalRandom rnd = ThreadLocalRandom.current();
		while (sb.length() < 16) {
			sb.append(rnd.nextInt(0, 10));
		}
		return sb.toString();
	}

	// ✅ 开户（自动生成卡号）
	@Override
	public Account register(Account account) {
		if (account.getCard() == null || account.getCard().isEmpty()) {
			String card;
			do {
				card = generateCard();
			} while (mapper.findByCard(card) != null);
			account.setCard(card);
		}

		mapper.insert(account);
		return mapper.findByCard(account.getCard());
	}

	// ✅ 登录（保留你原本的【密码解密】逻辑）
	@Override
	public Account login(String card, String password) {
		Account account = mapper.login(card);
		if (account == null) {
			throw new RuntimeException("卡号不存在");
		}
		Map<String, Object> claims = new HashMap<>();
		claims.put("card", account.getCard());
		claims.put("name",account.getName());
		String token = JwtUtils.generateToken(claims);
		account.setToken(token);
		String decryptedPassword;
		try {
			decryptedPassword = PasswordUtils.decryptPassword(password);
		} catch (IllegalArgumentException iae) {
			log.error("加密配置错误:{}", iae.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		} catch (Exception e) {
			log.error("密码解密失败{}", e.getMessage());
			throw new CryptoServiceException("服务器内部错误");
		}
		if (!account.getPassword().equals(decryptedPassword)) {
			throw new RuntimeException("密码错误");
		}
		return account;
	}

	@Override
	public Account deposit(DepositRequest req) {
		Account a = mapper.findByCard(req.getCard());
		if (a == null) throw new RuntimeException("账户不存在");

		double newBalance = a.getBalance() + req.getAmount();//修改1
		mapper.updateBalance(req.getCard(), newBalance);

		// ⭐ 核心：手动调用流水记录
		transactionService.record(req.getCard(), "DEPOSIT", req.getAmount(), "存款");

		a.setBalance(newBalance);
		return a;
	}

	// ✅ 取款逻辑补全
	@Override
	public Account withdraw(WithdrawRequest req) {
		Account a = mapper.findByCard(req.getCard());
		if (a == null) throw new RuntimeException("账户不存在");

		if (a.getBalance() < req.getAmount()) throw new RuntimeException("余额不足");

		double newBalance = a.getBalance() - req.getAmount();//修改2
		mapper.updateBalance(req.getCard(), newBalance);

		// ⭐ 核心：手动调用流水记录
		transactionService.record(req.getCard(), "WITHDRAW", req.getAmount(), "取款");

		a.setBalance(newBalance);
		return a;
	}

	@Override
	public Account transfer(TransferRequest req) {
		Account from = mapper.findByCard(req.getFromCard());
		Account to = mapper.findByCard(req.getToCard());

		if (from == null || to == null)
			throw new RuntimeException("账户不存在");

		if (from.getBalance() < req.getAmount())
			throw new RuntimeException("余额不足");

		double fromNew = from.getBalance() - req.getAmount();//修改3
		double toNew = to.getBalance() + req.getAmount();// 修改4

		mapper.updateBalance(req.getFromCard(), fromNew);
		mapper.updateBalance(req.getToCard(), toNew);

		from.setBalance(fromNew);
		to.setBalance(toNew);

		return from;
	}


	@Override
	public boolean changePassword(String card, String oldPwd, String newPwd) {
		Account a = mapper.findByCard(card);
		if (a == null) throw new RuntimeException("用户不存在");

		if (!a.getPassword().equals(oldPwd))
			throw new RuntimeException("旧密码错误");

		mapper.updatePassword(card, newPwd);
		return true;
	}

	@Override
	public Account getInfo(String card) {
		return mapper.findByCard(card);
	}
}

package com.example.atm.controller;

import com.example.atm.common.Result;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;

import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/atm")
public class AccountController {
	private final AccountService service;
	private final TransactionService transactionService;

	// 开户
	@PostMapping("/register")
	public Result register(@RequestBody Account req) {
		Account acc = service.register(req);
		return Result.ok(acc);
	}

	/* --- 登录 --- */
	@PostMapping("/login")
	public Result login(@RequestBody Account req) {
		Account acc = service.login(req.getCard(), req.getPassword());
		if (acc == null) {
			return Result.err("卡号或密码错误");
		}
		return Result.ok(acc);
	}

	/* --- 获取账户信息 --- */
	@GetMapping("/info")
	public Result info(@RequestParam String card) {
		Account acc = service.getInfo(card);
		if (acc == null) {
			return Result.err("账号不存在");
		}
		return Result.ok(acc);
	}

	/* --- 存款 --- */
	@PostMapping("/deposit")
	public Result deposit(@RequestBody DepositRequest req) {
		Account account = service.deposit(req);
		return Result.ok(account);
	}


	/* --- 取款 --- */
	@PostMapping("/withdraw")
	public Result withdraw(@RequestBody WithdrawRequest req) {
		Account account = service.withdraw(req);

		return Result.ok(account);
	}


	/* --- 转账 --- */
	@PostMapping("/transfer")
	public Result transfer(@RequestBody TransferRequest req) {

		Account transfer = service.transfer(req);

		Account from = service.getInfo(req.getFromCard());
		Account to = service.getInfo(req.getToCard());

		if (from == null || to == null) {
			return Result.err("转账账户不存在");
		}

		transactionService.recordTransfer(
				req.getFromCard(),
				req.getToCard(),
				req.getAmount(),
				from.getName(),
				to.getName()
		);

		return Result.ok(transfer);
	}

	/* --- 修改密码 --- */
	@PostMapping("/change")
	public Result changePassword(@RequestBody java.util.Map<String, String> req) {
		String card = req.get("card");
		String oldPwd = req.get("oldPwd");
		String newPwd = req.get("newPwd");

		boolean ok = service.changePassword(card, oldPwd, newPwd);
		if (!ok) return Result.err("修改失败");

		return Result.ok("修改成功");
	}

	/* --- 查询最近 10 条交易记录 --- */
	@GetMapping("/transactions")
	public Result transactions(@RequestParam String card) {
		return Result.ok(transactionService.getByCard(card));
	}
}

package com.example.atm.controller;

import com.example.atm.common.Result;
import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.LoginResponse;
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
	private final AccountService accountService;
	private final TransactionService transactionService;

	@PostMapping("/register")
	public Result register(@RequestBody Account req) {
		Account acc = accountService.register(req);
		return Result.ok(acc);
	}

	@PostMapping("/login")
	public Result login(@RequestBody Account req) {
		// ✅ 返回 LoginResponse，不含密码
		LoginResponse resp = accountService.login(req.getCard(), req.getPassword());
		return Result.ok(resp);
	}

	@GetMapping("/info")
	public Result info(@RequestParam String card) {
		Account acc = accountService.getInfo(card);
		if (acc == null) return Result.err("账号不存在");
		acc.setPassword(null); // ✅ 查询信息时也不返回密码
		return Result.ok(acc);
	}

	@PostMapping("/deposit")
	public Result deposit(@RequestBody DepositRequest req) {
		Account account = accountService.deposit(req);
		account.setPassword(null);
		return Result.ok(account);
	}

	@PostMapping("/withdraw")
	public Result withdraw(@RequestBody WithdrawRequest req) {
		Account account = accountService.withdraw(req);
		account.setPassword(null);
		return Result.ok(account);
	}

	@PostMapping("/transfer")
	public Result transfer(@RequestBody TransferRequest req) {
		Account transfer = accountService.transfer(req);

		Account from = accountService.getInfo(req.getFromCard());
		Account to = accountService.getInfo(req.getToCard());

		if (from == null || to == null) return Result.err("转账账户不存在");

		transactionService.recordTransfer(
				req.getFromCard(),
				req.getToCard(),
				req.getAmount(),
				from.getName(),
				to.getName()
		);

		transfer.setPassword(null);
		return Result.ok(transfer);
	}

	@PostMapping("/change")
	public Result changePassword(@RequestBody java.util.Map<String, String> req) {
		String card = req.get("card");
		String oldPwd = req.get("oldPwd");
		String newPwd = req.get("newPwd");

		boolean ok = accountService.changePassword(card, oldPwd, newPwd);
		if (!ok) return Result.err("修改失败");
		return Result.ok("修改成功");
	}

	@GetMapping("/transactions")
	public Result transactions(@RequestParam String card) {
		return Result.ok(transactionService.getByCard(card));
	}
}
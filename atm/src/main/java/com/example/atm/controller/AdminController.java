package com.example.atm.controller;

import cn.hutool.core.bean.BeanUtil;
import com.example.atm.common.Result;
import com.example.atm.dto.InfoResponse;
import com.example.atm.entity.Account;
import com.example.atm.service.AdminService;
import lombok.AllArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@AllArgsConstructor
@RequestMapping("/api/atm/admin")
public class AdminController {
	private AdminService adminService;

	@GetMapping("/lock")
	public Result findAllLockedAccounts() {
		return Result.ok(adminService.findAllLockedAccounts());
	}

	@PostMapping("/unlock")
	public Result unlock(@RequestBody Account account) {
		String card = account.getCard();
		adminService.unlock(card);
		return Result.ok("解锁成功");
	}
	@PostMapping("/limit")
	public Result limit(@RequestBody Account account) {
		String card = account.getCard();
		BigDecimal max = account.getMaxLimit();
		BigDecimal min = account.getMinLimit();
		adminService.limit(card,max,min);
		return Result.ok("额度限制设置成功");
	}
	@GetMapping("/limit")
	public Result findLimit(@RequestParam String card) {
		Account account = adminService.findLimitByCard(card);
		InfoResponse response = BeanUtil.copyProperties(account, InfoResponse.class);
		return Result.ok(response);
	}
}

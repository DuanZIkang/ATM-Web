package com.example.atm.controller;

import com.example.atm.common.Result;

import com.example.atm.entity.Account;
import com.example.atm.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/atm/admin")
public class AdminController {
	private AdminService adminService;
	@GetMapping("/lock")
	public Result findAllLockedAccounts(){
		return Result.ok(adminService.findAllLockedAccounts());
	}
	@PostMapping("/unlock")
	public Result unlock(@RequestBody Account account){
		String card = account.getCard();
		adminService.unlock(card);
		return Result.ok("解锁成功");
	}
}

package com.example.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
	private String card;
	private String name;
	private String sex;
	private double dailyLimit;
	private double balance;
	private String token;
	// ✅ 故意没有 password 字段
}
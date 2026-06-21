package com.example.atm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoginResponse {
	private String card;
	private String name;
	private String sex;
	private BigDecimal dailyLimit;
	private BigDecimal balance;
	private String token;
}
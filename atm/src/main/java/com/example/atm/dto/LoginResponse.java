package com.example.atm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LoginResponse {
	@NotNull(message = "卡号不能为空")
	private String card;
	private String name;
	private String sex;
	private BigDecimal dailyLimit;
	private BigDecimal balance;
	private String token;
}
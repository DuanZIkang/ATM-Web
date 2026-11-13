package com.example.atm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class InfoResponse {
	@NotNull(message = "卡号不能为空")
	private String card;
	private String name;
	// 数据库 sex 字段
	private String sex;
	// 数据库 limit 字段
	private BigDecimal dailyLimit;
	private BigDecimal balance;
	private String role;
	private BigDecimal maxLimit;
	private BigDecimal minLimit;
}

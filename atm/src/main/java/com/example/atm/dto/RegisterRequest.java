package com.example.atm.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RegisterRequest {
	@NotBlank(message = "姓名不能为空")
	@Size(max = 20, message = "姓名不超过 20 个字")
	private String name;

	@NotBlank(message = "性别不能为空")
	@Pattern(regexp = "[男女]", message = "性别只能是男或女")
	private String sex;

	// AES 加密后的密码，解密后必须是 6 位数字（在 Service 里校验）
	@NotBlank(message = "密码不能为空")
	private String password;

	@NotNull(message = "每日限额不能为空")
	@DecimalMin(value = "100", message = "每日限额不能低于 100 元")
	@DecimalMax(value = "50000", message = "每日限额不能超过 50000 元")
	private BigDecimal dailyLimit;
}
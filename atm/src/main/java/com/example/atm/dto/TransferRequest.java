package com.example.atm.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotBlank(message = "源卡号不能为空")
    private String fromCard;
    
    @NotBlank(message = "目标卡号不能为空")
    private String toCard;
    
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Digits(integer = 10, fraction = 2, message = "金额格式错误：仅允许纯数字，最多10位整数2位小数")
    private BigDecimal amount;
    
    @NotBlank(message = "密码不能为空")
    private String password;
}

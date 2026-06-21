package com.example.atm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    private String card;
    private BigDecimal amount;
    private String password;
}

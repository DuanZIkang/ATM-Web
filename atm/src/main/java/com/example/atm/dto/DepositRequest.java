package com.example.atm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRequest {
    private String card;
    private BigDecimal amount;
}

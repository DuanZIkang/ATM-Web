package com.example.atm.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String fromCard;
    private String toCard;
    private BigDecimal amount;
    private String password;
}

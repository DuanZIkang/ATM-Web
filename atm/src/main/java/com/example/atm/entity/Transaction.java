package com.example.atm.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
public class Transaction {

    private Long id;
    private String card;
    private String type;
    private BigDecimal amount;
    private String remark;

    // 新增字段：用于显示对方账户名称
    private String toName;

    private LocalDateTime time;
}

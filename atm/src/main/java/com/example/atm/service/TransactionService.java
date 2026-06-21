package com.example.atm.service;

import com.example.atm.entity.Transaction;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionService {

    void record(String card, String type, BigDecimal amount, String remark);

    // 新增：专用于转账（需要对方名字）
    void recordTransfer(String fromCard, String toCard, BigDecimal amount, String fromName, String toName);

    void trimOld(String card);
    List<Transaction> getByCard(String card);
}

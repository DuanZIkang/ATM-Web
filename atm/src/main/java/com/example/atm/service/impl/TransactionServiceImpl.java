package com.example.atm.service.impl;

import com.example.atm.entity.Transaction;
import com.example.atm.mapper.TransactionMapper;
import com.example.atm.service.TransactionService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionMapper mapper;

    public TransactionServiceImpl(TransactionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void record(String card, String type, BigDecimal amount, String remark) {
        Transaction t = new Transaction();
        t.setCard(card);
        t.setType(type);
        t.setAmount(amount);
        t.setRemark(remark);
        t.setTime(LocalDateTime.now());
        mapper.insert(t);
        trimOld(card);
    }

    @Override
    public void recordTransfer(String fromCard, String toCard, BigDecimal amount, String fromName, String toName) {

        // 转出记录：显示“转账给：对方姓名”
        Transaction out = new Transaction();
        out.setCard(fromCard);
        out.setType("TRANSFER_OUT");
        out.setAmount(amount);
        out.setRemark("转账给：" + toName);
        out.setToName(toName);
        out.setTime(LocalDateTime.now());
        mapper.insert(out);
        trimOld(fromCard);

        // 转入记录：显示“来自：对方姓名”
        Transaction in = new Transaction();
        in.setCard(toCard);
        in.setType("TRANSFER_IN");
        in.setAmount(amount);
        in.setRemark("来自：" + fromName);
        in.setToName(fromName);
        in.setTime(LocalDateTime.now());
        mapper.insert(in);
        trimOld(toCard);
    }

    @Override
    public List<Transaction> getByCard(String card) {
        return mapper.findByCard(card);
    }

    @Override
    public void trimOld(String card) {
        mapper.trimOldRecords(card);
    }
}

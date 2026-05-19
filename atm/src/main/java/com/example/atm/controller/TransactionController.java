package com.example.atm.controller;

import com.example.atm.entity.Transaction;
import com.example.atm.mapper.TransactionMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionMapper mapper;

    public TransactionController(TransactionMapper mapper) {
        this.mapper = mapper;
    }

    @GetMapping("/list")
    public List<Transaction> list(@RequestParam String card) {
        return mapper.findByCard(card);
    }
}

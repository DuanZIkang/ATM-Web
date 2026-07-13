package com.example.atm.controller;

import com.example.atm.entity.Transaction;

import com.example.atm.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@AllArgsConstructor
public class TransactionController {
    private final TransactionService service;


    @GetMapping("/list")
    public List<Transaction> list(@RequestParam String card) {
        return service.getRecentTransactions(card);
    }
    @GetMapping("/all")
    public List<Transaction> getAllTransactions(@RequestParam String card) {
        return service.getAllTransactions(card);
    }
}

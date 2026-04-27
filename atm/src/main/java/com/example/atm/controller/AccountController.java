package com.example.atm.controller;

import com.example.atm.dto.DepositRequest;
import com.example.atm.dto.TransferRequest;
import com.example.atm.dto.WithdrawRequest;
import com.example.atm.entity.Account;
import com.example.atm.entity.Transaction;
import com.example.atm.service.AccountService;
import com.example.atm.service.TransactionService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import com.example.atm.common.ApiResponse;


import java.util.List;

@RestController
@RequestMapping("/api/atm")
public class AccountController {

    private final AccountService service;
    private final TransactionService transactionService;

    public AccountController(AccountService service, TransactionService transactionService) {
        this.service = service;
        this.transactionService = transactionService;
    }

    /* --- 统一返回结构 --- */
    @Data
    public static class Result<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> Result<T> ok(T data) {
            Result<T> r = new Result<>();
            r.success = true;
            r.data = data;
            return r;
        }

        public static <T> Result<T> fail(String msg) {
            Result<T> r = new Result<>();
            r.success = false;
            r.message = msg;
            return r;
        }
    }

    /* --- 全局业务异常 --- */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntime(RuntimeException ex) {
        return Result.fail(ex.getMessage());
    }
    // 开户
    @PostMapping("/register")
    public Result<Account> register(@RequestBody Account req) {
        Account acc = service.register(req);
        return Result.ok(acc);
    }

    /* --- 登录 --- */
    @PostMapping("/login")
    public Result<Account> login(@RequestBody Account req) {
        Account acc = service.login(req.getCard(), req.getPassword());
        if (acc == null) {
            return Result.fail("卡号或密码错误");
        }
        return Result.ok(acc);
    }

    /* --- 获取账户信息 --- */
    @GetMapping("/info")
    public Result<Account> info(@RequestParam String card) {
        Account acc = service.getInfo(card);
        if (acc == null) {
            return Result.fail("账号不存在");
        }
        return Result.ok(acc);
    }

    /* --- 存款 --- */
    @PostMapping("/deposit")
    public ApiResponse<Account> deposit(@RequestBody DepositRequest req) {
        Account account = service.deposit(req);
        
        return ApiResponse.success(account);
    }


    /* --- 取款 --- */
    @PostMapping("/withdraw")
    public ApiResponse<Account> withdraw(@RequestBody WithdrawRequest req) {
        Account account = service.withdraw(req);

        return ApiResponse.success(account);
    }


    /* --- 转账 --- */
    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody TransferRequest req) {

        service.transfer(req);

        Account from = service.getInfo(req.getFromCard());
        Account to = service.getInfo(req.getToCard());

        if (from == null || to == null) {
            return Result.fail("转账账户不存在");
        }

        transactionService.recordTransfer(
                req.getFromCard(),
                req.getToCard(),
                req.getAmount(),
                from.getName(),
                to.getName()
        );

        return Result.ok("OK");
    }

    /* --- 修改密码 --- */
    @PostMapping("/change")
    public Result<String> changePassword(@RequestBody java.util.Map<String, String> req) {
        String card = req.get("card");
        String oldPwd = req.get("oldPwd");
        String newPwd = req.get("newPwd");

        boolean ok = service.changePassword(card, oldPwd, newPwd);
        if (!ok) return Result.fail("修改失败");

        return Result.ok("修改成功");
    }

    /* --- 查询最近 10 条交易记录 --- */
    @GetMapping("/transactions")
    public Result<List<Transaction>> transactions(@RequestParam String card) {
        return Result.ok(transactionService.getByCard(card));
    }
}

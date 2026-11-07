package com.example.atm.service;

import com.example.atm.dto.*;
import com.example.atm.entity.Account;

public interface AccountService {

    LoginResponse login(String card, String password) ;

    Account register(RegisterRequest req);

    Account deposit(DepositRequest req);

    Account withdraw(WithdrawRequest req);

    Account transfer(TransferRequest req);

    boolean changePassword(String card, String oldPwd, String newPwd);

    Account getInfo(String card);

    void closeAccount(String card, String password);
}
package com.example.atm.service;

import com.example.atm.dto.*;
import com.example.atm.entity.Account;

public interface AccountService {

    Account login(String card, String password);

    Account register(Account account);

    Account deposit(DepositRequest req);

    Account withdraw(WithdrawRequest req);

    Account transfer(TransferRequest req);

    boolean changePassword(String card, String oldPwd, String newPwd);

    Account getInfo(String card);
}

package com.example.atm.service;

import com.example.atm.dto.LockResponse;
import com.example.atm.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AdminService {
	List<LockResponse> findAllLockedAccounts();

	void unlock(String card);

	void limit(String card, BigDecimal max, BigDecimal min);

	Account findLimitByCard(String card);
}

package com.example.atm.service;

import com.example.atm.dto.LockResponse;


import java.util.List;

public interface AdminService {
	List<LockResponse> findAllLockedAccounts();

	void unlock(String card);
}

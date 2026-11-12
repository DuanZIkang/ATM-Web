package com.example.atm.service.impl;

import com.example.atm.common.exception.AccountNotLockedException;
import com.example.atm.dto.LockResponse;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.mapper.AdminMapper;
import com.example.atm.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
	private final AdminMapper adminMapper;
	private final AccountMapper accountMapper;

	@Override
	public List<LockResponse> findAllLockedAccounts() {
		log.info("查询所有的锁定账号");
		return adminMapper.findAllLockedAccounts();
	}

	@Override
	public void unlock(String card) {
		log.info("解锁指定账号{}", card);
		Account account = accountMapper.findByCard(card);
		if (!account.getStatus().equals("LOCKED")){
			throw new AccountNotLockedException("该账号未锁定");
		}
		adminMapper.unlock(card);
	}
}

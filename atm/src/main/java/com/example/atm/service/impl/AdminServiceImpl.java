package com.example.atm.service.impl;

import com.example.atm.common.exception.AccountNotLockedException;
import com.example.atm.common.exception.BalanceException;
import com.example.atm.common.exception.BalanceNotZeroException;
import com.example.atm.dto.LockResponse;
import com.example.atm.entity.Account;
import com.example.atm.mapper.AccountMapper;
import com.example.atm.mapper.AdminMapper;
import com.example.atm.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
		if (!account.getStatus().equals("LOCKED")) {
			throw new AccountNotLockedException("该账号未锁定");
		}
		adminMapper.unlock(card);
	}

	@Override
	public void limit(String card, BigDecimal max, BigDecimal min) {
		log.info("设置账号{}的额度限制", card);

		// 1. 参数校验
		if (card == null || card.trim().isEmpty()) {
			throw new IllegalArgumentException("卡号不能为空");
		}
		if (max == null || min == null) {
			throw new IllegalArgumentException("额度参数不能为空");
		}
		if (max.compareTo(BigDecimal.ZERO) <= 0 || min.compareTo(BigDecimal.ZERO) <= 0) {
			throw new BalanceNotZeroException("额度不能为负数或零");
		}
		if (max.compareTo(min) <= 0) {
			throw new BalanceException("最大额度必须大于最小额度");
		}

		// 2. 账号存在性检查
		Account account = accountMapper.findByCard(card);
		if (account == null) {
			throw new IllegalArgumentException("账号不存在");
		}

		// 3. 获取当前限额并调整（如果超出新范围）
		BigDecimal currentLimit = account.getDailyLimit();
		if (currentLimit != null) {
			if (currentLimit.compareTo(max) > 0) {
				log.warn("当前限额{}超过新的最大值{}，强制调整为{}", currentLimit, max, max);
				accountMapper.changeLimit(card, max);
			} else if (currentLimit.compareTo(min) < 0) {
				log.warn("当前限额{}低于新的最小值{}，强制调整为{}", currentLimit, min, min);
				accountMapper.changeLimit(card, min);
			}
		} else {
			// 当前限额为空，设置为最小值
			log.info("当前限额为空，设置为最小值{}", min);
			accountMapper.changeLimit(card, min);
		}

		// 4. 更新限额范围
		adminMapper.limit(card, max, min);
	}

	@Override
	public Account findLimitByCard(String card) {
		return accountMapper.findByCard(card);
	}

}

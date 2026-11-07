package com.example.atm.common.exception;

public class DailyLimitExceededException extends BalanceException {
	public DailyLimitExceededException(String message) {
		super(message);
	}
}

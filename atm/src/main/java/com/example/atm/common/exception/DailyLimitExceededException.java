package com.example.atm.common.exception;

public class DailyLimitExceededException extends AccountException {
	public DailyLimitExceededException(String message) {
		super(message);
	}
}

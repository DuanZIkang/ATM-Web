package com.example.atm.common.exception;

public class AccountLockedException extends AccountException {
	public AccountLockedException(String message) {
		super(message);
	}
}

package com.example.atm.common.exception;

public class AccountLockedException extends LoginException {
	public AccountLockedException(String message) {
		super(message);
	}
}

package com.example.atm.common.exception;

public class AccountNotLockedException extends AccountException {
	public AccountNotLockedException(String message) {
		super(message);
	}
}

package com.example.atm.common.exception;

public class BalanceNotZeroException extends AccountException {
	public BalanceNotZeroException(String message) {
		super(message);
	}
}

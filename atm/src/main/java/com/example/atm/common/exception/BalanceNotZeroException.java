package com.example.atm.common.exception;

public class BalanceNotZeroException extends BalanceException {
	public BalanceNotZeroException(String message) {
		super(message);
	}
}

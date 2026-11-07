package com.example.atm.common.exception;

public class InsufficientBalanceException extends BalanceException {
	public InsufficientBalanceException(String message) {
		super(message);
	}
}

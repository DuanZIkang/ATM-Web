package com.example.atm.common.exception;

public class InsufficientBalanceException extends AccountException {
	public InsufficientBalanceException(String message) {
		super(message);
	}
}

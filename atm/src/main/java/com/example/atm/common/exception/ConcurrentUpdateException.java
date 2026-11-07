package com.example.atm.common.exception;

public class ConcurrentUpdateException extends AccountException {
	public ConcurrentUpdateException(String message) {
		super(message);
	}
}

package com.example.atm.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result {
	private boolean success;
	private String message;
	private Object data;

	public static Result ok(Object data) {
		Result r = new Result();
		r.success = true;
		r.data = data;
		return r;
	}

	public static Result ok() {
		Result r = new Result();
		r.success = true;
		return r;
	}


	public static Result err(String msg) {
		Result r = new Result();
		r.success = false;
		r.message = msg;
		return r;
	}
}

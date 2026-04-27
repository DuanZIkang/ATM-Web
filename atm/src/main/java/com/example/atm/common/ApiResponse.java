package com.example.atm.common;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ApiResponse<T> {

	private boolean success;
	private String message;
	private T data;

	public ApiResponse() {}

	public ApiResponse(boolean success, String message, T data) {
		this.success = success;
		this.message = message;
		this.data = data;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "ok", data);
	}

	public static <T> ApiResponse<T> fail(String message) {
		return new ApiResponse<>(false, message, null);
	}

	// ===== Getter / Setter =====

}

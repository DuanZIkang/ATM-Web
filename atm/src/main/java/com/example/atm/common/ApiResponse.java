package com.example.atm.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	private boolean success;
	private String message;
	private T data;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, "ok", data);
	}

}

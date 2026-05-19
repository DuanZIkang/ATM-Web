package com.example.atm.common.exception;

import com.example.atm.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public Result handleRuntime(RuntimeException ex) {
		return Result.err(ex.getMessage());
	}

}

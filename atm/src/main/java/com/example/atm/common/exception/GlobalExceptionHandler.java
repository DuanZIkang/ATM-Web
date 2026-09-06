package com.example.atm.common.exception;

import com.example.atm.common.Result;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 JSON 反序列化失败（最高优先级）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("JSON 反序列化失败: {}", ex.getMessage(), ex);

        Throwable cause = ex.getCause();
        if (cause instanceof JsonMappingException) {
            String message = cause.getMessage();
            if (message.contains("BigDecimal")) {
                return Result.err("金额格式错误：仅允许输入纯数字");
            }
        }

        return Result.err("请求数据格式错误");
    }

    /**
     * 处理 JSR-380 验证失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValidationException(MethodArgumentNotValidException ex) {
        // 获取第一个错误信息
        String message = Objects.requireNonNull(ex.getBindingResult()
				        .getFieldError())
                .getDefaultMessage();
        
        log.warn("参数验证失败: {}", message);
        return Result.err(message);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(DataFormatException.class)
    public Result handleDataFormatException(DataFormatException ex) {
        log.warn("数据格式异常: {}", ex.getMessage());
        return Result.err(ex.getMessage());
    }

    /**
     * 处理账户异常
     */
    @ExceptionHandler(AccpuntException.class)
    public Result handleAccountException(AccpuntException ex) {
        log.warn("账户异常: {}", ex.getMessage());
        return Result.err(ex.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception ex) {
        log.error("服务器错误: ", ex);
        return Result.err("服务器内部错误");
    }

}

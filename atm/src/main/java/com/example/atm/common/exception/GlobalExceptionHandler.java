package com.example.atm.common.exception;

import com.example.atm.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

/**
 * 全局异常处理。状态码约定：
 * <pre>
 * 400 请求格式/参数错误          DataFormatException、JSON 解析失败、参数校验失败
 * 401 登录失败                   LoginFailedException（登录态过期由 TokenInterceptor 直接返回 401）
 * 403 无权操作他人账户           ForbiddenException
 * 404 账户/资源不存在            AccountNotFoundException、路径不存在
 * 405 请求方法不对               如对 POST 接口发 GET
 * 415 Content-Type 不支持
 * 422 业务规则不允许             InsufficientBalanceException、DailyLimitExceededException、PasswordComparisonException
 * 423 账户被锁定                 AccountLockedException
 * 500 服务端内部错误             CryptoServiceException 及其它未预期异常
 * </pre>
 * 注意：已登录用户的业务失败（如"旧密码错误"）不能用 401，
 * 否则前端的 401 拦截器会把用户当成登录过期并踢回登录页，所以用 422。
 * Spring 按"异常类型最接近"选择处理器，与下面方法的书写顺序无关。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ================================================================ 400：请求本身有问题

    /**
     * JSON 反序列化失败。外部随意发来的垃圾数据都会触发，只记 warn，不打堆栈。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("JSON 反序列化失败: {}", ex.getMessage());

        // 不依赖具体的 Jackson 异常类（Jackson 2/3 的类名和包名不同），只看错误信息
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null && cause.getMessage().contains("BigDecimal")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Result.err("金额格式错误：仅允许输入纯数字"));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.err("请求数据格式错误"));
    }

    /**
     * JSR-380 参数校验失败
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidationException(MethodArgumentNotValidException ex) {
        // 取第一个错误信息；类级别校验没有 FieldError，不能直接 requireNonNull
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage()
                : ex.getBindingResult().getAllErrors().stream()
                        .findFirst().map(ObjectError::getDefaultMessage).orElse(null);
        if (message == null) {
            message = "请求参数错误";
        }

        log.warn("参数验证失败: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.err(message));
    }

    /**
     * 缺少请求参数 / 参数类型不对（如 ?card 缺失）
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Result> handleBadParameter(Exception ex) {
        log.warn("请求参数错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.err("请求参数错误"));
    }

    /**
     * 数据格式异常：密码格式不对、新密码不是 6 位数字、金额不是正数
     */
    @ExceptionHandler(DataFormatException.class)
    public ResponseEntity<Result> handleDataFormatException(DataFormatException ex) {
        log.warn("数据格式异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.err(ex.getMessage()));
    }

    // ================================================================ 401 / 403：身份与权限

    /**
     * 登录失败：卡号不存在和密码错误都走这里，状态码与提示必须一致，防止枚举卡号。
     */
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<Result> handleLoginFailed(LoginFailedException ex) {
        log.warn("登录失败: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.err(ex.getMessage()));
    }

    /**
     * 越权访问：token 有效，但要操作的卡号不是自己的。
     * （没带 token / token 失效属于 401，由 TokenInterceptor 直接处理）
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Result> handleForbidden(ForbiddenException ex) {
        log.warn("越权访问: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.err(ex.getMessage()));
    }

    // ================================================================ 404 / 405 / 415：路径、方法、格式

    /**
     * 路径不存在。扫描器会大量触发，只记 warn。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result> handleNotFound(NoResourceFoundException ex) {
        log.warn("资源不存在: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.err("资源不存在"));
    }

    /**
     * 请求方法不对（例如对 POST 接口发了 GET）。同样是扫描器常客，只记 warn。
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持: {}", ex.getMessage());
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED);
        Set<HttpMethod> supported = ex.getSupportedHttpMethods();
        if (supported != null && !supported.isEmpty()) {
            builder.allow(supported.toArray(new HttpMethod[0]));
        }
        return builder.body(Result.err("请求方法不支持"));
    }

    /**
     * Content-Type 不支持（接口要 JSON，却收到别的格式）
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Result> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Content-Type 不支持: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(Result.err("不支持的请求格式，请使用 application/json"));
    }

    // ================================================================ 账户业务异常

    /**
     * 账户不存在（如转账的目标卡号不存在）
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Result> handleAccountNotFound(AccountNotFoundException ex) {
        log.warn("账户不存在: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.err(ex.getMessage()));
    }

    /**
     * 业务规则不允许：余额相关异常（超出每日限额，注销时余额不为零和余额不足）、旧密码错误。
     * 请求格式没问题，是当前状态/数据不满足条件，所以是 422 而不是 400。
     */
    @ExceptionHandler({BalanceException.class,
            PasswordComparisonException.class})
    public ResponseEntity<Result> handleBusinessRule(AccountException ex) {
        log.warn("业务规则不允许: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(Result.err(ex.getMessage()));
    }

    /**
     * 账户被锁定（登录失败次数过多的临时锁定，或需要管理员解锁的永久锁定）
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Result> handleAccountLocked(AccountLockedException ex) {
        log.warn("账户锁定: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(Result.err(ex.getMessage()));
    }

    /**
     * 加解密服务异常：属于服务端问题，返回 500。
     * 具体原因已在 service 里记录，这里不向客户端透露细节。
     */
    @ExceptionHandler(CryptoServiceException.class)
    public ResponseEntity<Result> handleCryptoService(CryptoServiceException ex) {
        log.error("加解密服务异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.err("服务器内部错误"));
    }

    /**
     * 其它未单独处理的账户异常，保持原来的 400。
     */
    @ExceptionHandler(AccountException.class)
    public ResponseEntity<Result> handleAccountException(AccountException ex) {
        log.warn("账户异常: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.err(ex.getMessage()));
    }

    // ================================================================ 500：兜底

    /**
     * 其他未预期异常：只有这里才打 ERROR 加完整堆栈。
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception ex) {
        log.error("服务器错误: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.err("服务器内部错误"));
    }

}

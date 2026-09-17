package com.xb.springai.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.UUID;

/**
 * 全局异常处理器 —— 统一拦截 Controller 层异常，返回结构化错误响应
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p><b>实战价值</b>：生产系统不能让 Spring 默认白页暴露给前端。全局异常处理器做到三件事：</p>
 * <ol>
 *   <li><b>统一格式</b>：所有错误都返回 {code, message, requestId} 结构</li>
 *   <li><b>分类处理</b>：校验异常 → 400、业务异常 → 对应状态码、未知异常 → 500</li>
 *   <li><b>可追踪</b>：每次错误带 requestId，方便日志关联排查</li>
 * </ol>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.error(400, field);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[{}] 未处理异常", requestId, ex);
        return ApiResponse.error(500, "服务内部错误，requestId=" + requestId);
    }
}

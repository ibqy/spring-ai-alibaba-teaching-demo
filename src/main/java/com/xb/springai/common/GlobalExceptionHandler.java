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
 *
 * @author ibqy
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理参数校验异常（如 @NotBlank、@Valid 校验失败）。
     *
     * <p>提取第一个出错字段及其校验消息，返回 400 状态码。</p>
     *
     * @param ex 校验异常
     * @return 包含字段错误信息的统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.error(400, field);
    }

    /**
     * 处理业务参数非法异常。
     *
     * @param ex 非法参数异常
     * @return 400 状态码的统一响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.error(400, ex.getMessage());
    }

    /**
     * 兜底处理所有未预期的异常。
     *
     * <p>生成短 requestId 便于日志追踪，同时避免向用户暴露异常堆栈细节。</p>
     *
     * @param ex 未处理的异常
     * @return 500 状态码的统一响应，附带 requestId
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception ex) {
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        log.error("[{}] 未处理异常", requestId, ex);
        return ApiResponse.error(500, "服务内部错误，requestId=" + requestId);
    }
}

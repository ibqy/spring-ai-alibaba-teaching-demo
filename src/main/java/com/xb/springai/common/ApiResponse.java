package com.xb.springai.common;

/**
 * 统一 API 响应包装 —— 所有接口返回统一 JSON 结构
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 *
 * <p><b>为什么要统一包装</b>：前端只需一种解析方式，错误码 + 消息 + 数据三段式是业界标准。
 * 成功时 code=0，失败时 code 为业务错误码，data 为 null。</p>
 *
 * <p>格式：<code>{"code": 0, "message": "ok", "data": ...}</code></p>
 */
public record ApiResponse<T>(
        int code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

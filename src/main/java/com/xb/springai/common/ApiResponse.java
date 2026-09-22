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
 *
 * @author ibqy
 */
public record ApiResponse<T>(
        int code,
        String message,
        T data
) {

    /**
     * 构造成功响应。
     *
     * @param data 需要返回给前端的业务数据
     * @param <T>  数据类型
     * @return code=0, message="ok" 的统一响应
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    /**
     * 构造失败响应。
     *
     * @param code    业务错误码
     * @param message 错误描述
     * @param <T>     数据类型（失败时为 null）
     * @return 带错误码和消息的统一响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

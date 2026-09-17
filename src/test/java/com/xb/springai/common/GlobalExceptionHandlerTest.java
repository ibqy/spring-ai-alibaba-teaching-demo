package com.xb.springai.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 统一响应包装 + 全局异常处理器测试
 *
 * <p>作者：ibqy | 日期：2026-09-17</p>
 */
class GlobalExceptionHandlerTest {

    @Test
    void apiResponseOk() {
        ApiResponse<String> resp = ApiResponse.ok("hello");
        assertEquals(0, resp.code());
        assertEquals("ok", resp.message());
        assertEquals("hello", resp.data());
    }

    @Test
    void apiResponseError() {
        ApiResponse<Void> resp = ApiResponse.error(400, "参数错误");
        assertEquals(400, resp.code());
        assertEquals("参数错误", resp.message());
        assertNull(resp.data());
    }

    @Test
    void apiResponseOkWithNull() {
        ApiResponse<Object> resp = ApiResponse.ok(null);
        assertEquals(0, resp.code());
        assertNull(resp.data());
    }

    @Test
    void handleValidationReturnsFieldError() throws Exception {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var target = new TestTarget();
        var bindingResult = new org.springframework.validation.BeanPropertyBindingResult(target, "target");
        bindingResult.rejectValue("message", "NotBlank", "message 不能为空");
        var method = TestTarget.class.getDeclaredMethod("setMessage", String.class);
        var methodParam = new org.springframework.core.MethodParameter(method, 0);
        var ex = new org.springframework.web.bind.MethodArgumentNotValidException(methodParam, bindingResult);

        ApiResponse<Void> resp = handler.handleValidation(ex);
        assertEquals(400, resp.code());
        assertTrue(resp.message().contains("message"));
    }

    static class TestTarget {
        private String message = "";
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Test
    void handleIllegalArgument() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiResponse<Void> resp = handler.handleIllegalArgument(
                new IllegalArgumentException("参数不合法"));
        assertEquals(400, resp.code());
        assertEquals("参数不合法", resp.message());
    }

    @Test
    void handleUnknownException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ApiResponse<Void> resp = handler.handleUnknown(new RuntimeException("boom"));
        assertEquals(500, resp.code());
        assertTrue(resp.message().contains("requestId="));
    }
}

package com.xb.springai.controller.demo10;

/**
 * 客服接口的请求体 —— 客户端 POST 的 JSON 结构
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p>字段：<code>conversationId</code> 标识一个顾客会话（用于记忆隔离），
 * <code>message</code> 是顾客说的话。</p>
 */
public record Demo10AskRequest(
        String conversationId,  // 会话 ID（可选，不传则用默认）
        String message          // 顾客消息（必填）
) {
}
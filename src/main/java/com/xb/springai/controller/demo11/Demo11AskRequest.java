package com.xb.springai.controller.demo11;

import jakarta.validation.constraints.NotBlank;

/**
 * demo11 · 订餐客服的请求体 —— 客户端 POST 的 JSON 结构
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>为什么拆成 record</b>：与 demo10.Demo10AskRequest 同样模式，让请求更结构化、可读。
 * 相比 demo08 的单个 query 参数，这里把"会话"和"是否联网"都作为一等公民，体现组合实战的完整输入面。</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *   <li>{@code conversationId}：会话 ID，用于记忆隔离（同一 ID 共享多轮上下文，不同 ID 互不干扰）。</li>
 *   <li>{@code message}：顾客说的话（必填）。</li>
 *   <li>{@code enableSearch}：是否开启 DashScope 联网搜索，处理"今天有什么实时新闻、快递到哪里"这类问题。</li>
 * </ul>
 */
public record Demo11AskRequest(
        String conversationId,  // 会话 ID（可选，不传用默认）
        @NotBlank(message = "message 不能为空")
        String message,         // 顾客消息（必填）
        Boolean enableSearch    // 是否联网搜索（可选，null 视为 false）
) {
}
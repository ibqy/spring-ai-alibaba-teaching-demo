package com.xb.springai.controller.demo14;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * demo14 · 生产运维：Token 用量可观测 —— 给 AI 应用装上"电表"
 *
 * <p>作者：ibqy | 日期：2026-10-16</p>
 *
 * <p><b>教学知识点</b>：</p>
 * <ol>
 *   <li><b>横切关注点用 Advisor 收口</b>：{@link TokenUsageAdvisor} 挂在 ChatClient 上，
 *       每次模型调用自动记账，业务端点零改动（对比：写进 Controller 会遍及 demo01~13）；</li>
 *   <li><b>用量数据来源</b>：DashScope 响应用量（prompt_tokens / completion_tokens）
 *       经 Spring AI 映射到 {@code ChatResponse.getMetadata().getUsage()}；</li>
 *   <li><b>成本估算</b>：按"元/百万 token"单价计算（application.yml 配置），
 *       单价未配置时按 0 展示并说明原因，不显示误导性数字；</li>
 *   <li><b>延迟分位</b>：P50/P95 比平均值更能反映"慢请求有多慢"。</li>
 * </ol>
 *
 * <p><b>运行示例</b>：</p>
 * <pre>
 * # 1) 产生几次调用（每次都会被自动计量）
 * curl "http://localhost:8080/api/demo14/chat?message=用一句话介绍通义千问"
 * curl "http://localhost:8080/api/demo14/chat?message=再写一句"
 *
 * # 2) 查看累计用量与估算成本
 * curl http://localhost:8080/api/demo14/usage
 *
 * # 3) 清零重新计量
 * curl -X POST http://localhost:8080/api/demo14/usage/reset
 * </pre>
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api/demo14")
public class Demo14TokenObservabilityController {

    private final TokenUsageMetrics metrics;
    private final ChatClient chatClient;

    /**
     * @param chatClientBuilder Spring Boot 自动装配的 ChatClient.Builder（SAA starter 提供）
     * @param metrics           计量器（demo14 包内 Bean）
     * @param promptPricePerMillion     输入单价（元/百万 token），默认 0 = 未配置
     * @param completionPricePerMillion 输出单价（元/百万 token），默认 0 = 未配置
     */
    public Demo14TokenObservabilityController(
            ChatClient.Builder chatClientBuilder,
            TokenUsageMetrics metrics,
            @Value("${demo14.prompt-price-per-million:0}") double promptPricePerMillion,
            @Value("${demo14.completion-price-per-million:0}") double completionPricePerMillion) {
        this.metrics = metrics;
        this.metrics.configurePrices(promptPricePerMillion, completionPricePerMillion);
        // 把计量 Advisor 挂到这个 ChatClient 上——演示"一次装配，自动记账"
        this.chatClient = chatClientBuilder
                .defaultAdvisors(new TokenUsageAdvisor(metrics))
                .defaultSystem("你是通义千问的演示助手，请用简洁的中文回答问题。")
                .build();
    }

    /**
     * GET /api/demo14/chat?message=…… —— 普通对话（每次调用自动计入用量）
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "用一句话介绍 Spring AI Alibaba") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * GET /api/demo14/usage —— 累计用量快照
     *
     * <p>字段：callCount / promptTokens / completionTokens / totalTokens /
     * avgTokensPerCall / llmLatencyP50Ms / llmLatencyP95Ms / estimatedCostYuan / priceNote。</p>
     */
    @GetMapping("/usage")
    public Map<String, Object> usage() {
        return metrics.snapshot();
    }

    /**
     * POST /api/demo14/usage/reset —— 清零重新计量（演示用）
     */
    @PostMapping("/usage/reset")
    public Map<String, Object> reset() {
        metrics.reset();
        return Map.of("status", "ok", "message", "计量已清零");
    }
}

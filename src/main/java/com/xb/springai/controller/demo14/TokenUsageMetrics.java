package com.xb.springai.controller.demo14;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Token 用量计量器 —— demo14 的记账核心（与 Spring/LLM 解耦，可离线单测）
 *
 * <p>作者：ibqy | 日期：2026-10-16</p>
 *
 * <p><b>为什么单独一个类而不写在 Advisor 里</b>：计量逻辑（累计、分位、成本、快照）
 * 是纯计算，Advisor 只负责"在调用前后触发表它"。拆开后：</p>
 * <ul>
 *   <li>计量规则可以用真实 {@link Usage} 对象离线单测（见 {@code TokenUsageMetricsTest}）</li>
 *   <li>换传输方式（SSE 推送/落 Redis）不动计量代码</li>
 * </ul>
 *
 * <p><b>三个容错分支都是实测踩出来的</b>：</p>
 * <ol>
 *   <li>Spring AI 1.x 的 {@code Usage.getPromptTokens()} 返回 {@code Integer}（可为 null），
 *       部分模型/代理不返回用量——判空按 0 计，不能 NPE；</li>
 *   <li>失败的调用也要记延迟——超时/报错的耗时往往正是排查线索；</li>
 *   <li>单价未配置时成本按 0 展示并附说明，而不是抛异常或显示误导性数字。</li>
 * </ol>
 *
 * <p><b>并发安全</b>：Advisor 是单例 Bean，并发请求同时写计数器。
 * 用 {@code LongAdder}（多写少读场景比 AtomicLong 争用小）；
 * 延迟样本用固定容量环形缓冲（多写），读时拷贝排序求分位（少读）。</p>
 *
 * @author ibqy
 */
@Component
public class TokenUsageMetrics {

    /** 延迟样本容量：教学场景样本量小，512 足够看分位趋势 */
    private static final int LATENCY_SAMPLE_CAP = 512;

    private final AtomicLong callCount = new AtomicLong();
    private final AtomicLong promptTokens = new AtomicLong();
    private final AtomicLong completionTokens = new AtomicLong();

    /** 延迟样本（毫秒）环形缓冲 */
    private final long[] latencySamples = new long[LATENCY_SAMPLE_CAP];
    private final AtomicLong latencyWriteIndex = new AtomicLong();
    private final AtomicLong latencyCount = new AtomicLong();

    /** 模型单价（元/百万 token），0 = 未配置 */
    private volatile double promptPricePerMillion;
    private volatile double completionPricePerMillion;

    /**
     * 记录一次模型调用
     *
     * @param durationNanos 调用耗时（纳秒）
     * @param usage         本次用量（可为 null，如调用失败）
     */
    public void record(long durationNanos, Usage usage) {
        callCount.incrementAndGet();
        recordLatency(durationNanos / 1_000_000);
        if (usage == null) return;
        if (usage.getPromptTokens() != null) {
            promptTokens.addAndGet(usage.getPromptTokens());
        }
        if (usage.getCompletionTokens() != null) {
            completionTokens.addAndGet(usage.getCompletionTokens());
        }
    }

    /** 延迟分位数（毫秒），无样本返回 -1 */
    public long latencyP50() { return percentile(0.50); }

    public long latencyP95() { return percentile(0.95); }

    private long percentile(double p) {
        long count = Math.min(latencyCount.get(), LATENCY_SAMPLE_CAP);
        if (count == 0) return -1;
        long[] copy = Arrays.copyOf(latencySamples, (int) count);
        Arrays.sort(copy);
        int idx = (int) Math.min(count - 1, Math.max(0, Math.ceil(p * count) - 1));
        return copy[idx];
    }

    private void recordLatency(long millis) {
        long idx = latencyWriteIndex.getAndIncrement() % LATENCY_SAMPLE_CAP;
        latencySamples[(int) idx] = millis;
        latencyCount.incrementAndGet();
    }

    /**
     * 配置模型单价（元/百万 token）
     *
     * @param promptPricePerMillion     输入单价
     * @param completionPricePerMillion 输出单价
     */
    public void configurePrices(double promptPricePerMillion, double completionPricePerMillion) {
        this.promptPricePerMillion = Math.max(0, promptPricePerMillion);
        this.completionPricePerMillion = Math.max(0, completionPricePerMillion);
    }

    /** 估算成本（元） */
    public double estimatedCostYuan() {
        return promptTokens.get() / 1_000_000.0 * promptPricePerMillion
                + completionTokens.get() / 1_000_000.0 * completionPricePerMillion;
    }

    /** 是否配置了单价（用于在响应里说明成本为何是 0） */
    public boolean priceConfigured() {
        return promptPricePerMillion > 0 || completionPricePerMillion > 0;
    }

    /** 清零（演示/测试用） */
    public void reset() {
        callCount.set(0);
        promptTokens.set(0);
        completionTokens.set(0);
        latencyWriteIndex.set(0);
        latencyCount.set(0);
        Arrays.fill(latencySamples, 0);
    }

    /**
     * 用量快照（GET /api/demo14/usage 的响应体）
     *
     * @return 有序 Map，字段含义见 {@code Demo14TokenObservabilityController}
     */
    public Map<String, Object> snapshot() {
        long calls = callCount.get();
        long prompt = promptTokens.get();
        long completion = completionTokens.get();
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("callCount", calls);
        snapshot.put("promptTokens", prompt);
        snapshot.put("completionTokens", completion);
        snapshot.put("totalTokens", prompt + completion);
        snapshot.put("avgTokensPerCall", calls == 0 ? 0 : (prompt + completion) / (double) calls);
        snapshot.put("llmLatencyP50Ms", latencyP50());
        snapshot.put("llmLatencyP95Ms", latencyP95());
        snapshot.put("estimatedCostYuan", estimatedCostYuan());
        snapshot.put("priceNote", priceConfigured()
                ? "单价（元/百万 token）：输入 " + promptPricePerMillion + " / 输出 " + completionPricePerMillion
                : "未配置模型单价（application.yml 的 demo14.*.price-per-million），成本按 0 展示");
        return snapshot;
    }
}

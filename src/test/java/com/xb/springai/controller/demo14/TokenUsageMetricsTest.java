package com.xb.springai.controller.demo14;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.metadata.DefaultUsage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TokenUsageMetrics 测试 —— 计量规则的离线验证
 *
 * 覆盖实测踩出来的容错分支：null 用量、null 字段、延迟分位、成本估算、单价未配置提示。
 *
 * @author ibqy
 */
@DisplayName("TokenUsageMetrics 用量计量")
class TokenUsageMetricsTest {

    private TokenUsageMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new TokenUsageMetrics();
    }

    @Test
    @DisplayName("正常记账：调用数 + 输入/输出 token")
    void recordsUsage() {
        metrics.record(1_000_000L, new DefaultUsage(120, 35));

        var snapshot = metrics.snapshot();
        assertThat(snapshot.get("callCount")).isEqualTo(1L);
        assertThat(snapshot.get("promptTokens")).isEqualTo(120L);
        assertThat(snapshot.get("completionTokens")).isEqualTo(35L);
        assertThat(snapshot.get("totalTokens")).isEqualTo(155L);
        assertThat(snapshot.get("avgTokensPerCall")).isEqualTo(155.0);
    }

    @Test
    @DisplayName("多次调用累计与均值")
    void accumulatesAcrossCalls() {
        metrics.record(1_000_000L, new DefaultUsage(100, 10));
        metrics.record(1_000_000L, new DefaultUsage(200, 20));

        var snapshot = metrics.snapshot();
        assertThat(snapshot.get("callCount")).isEqualTo(2L);
        assertThat(snapshot.get("totalTokens")).isEqualTo(330L);
        assertThat(snapshot.get("avgTokensPerCall")).isEqualTo(165.0);
    }

    @Test
    @DisplayName("null 用量容错：调用失败时只记次数与延迟，不 NPE")
    void nullUsageIsSafe() {
        metrics.record(2_000_000L, null);

        var snapshot = metrics.snapshot();
        assertThat(snapshot.get("callCount")).isEqualTo(1L);
        assertThat(snapshot.get("promptTokens")).isEqualTo(0L);
        assertThat(snapshot.get("totalTokens")).isEqualTo(0L);
    }

    @Test
    @DisplayName("延迟分位：多样本后 P50/P95 有值且有序")
    void latencyPercentiles() {
        for (int i = 1; i <= 100; i++) {
            metrics.record(i * 1_000_000L, new DefaultUsage(1, 1));  // 1ms ~ 100ms
        }

        assertThat(metrics.latencyP50()).isBetween(45L, 55L);
        assertThat(metrics.latencyP95()).isBetween(90L, 100L);
        assertThat(metrics.latencyP95()).isGreaterThanOrEqualTo(metrics.latencyP50());
    }

    @Test
    @DisplayName("无样本时分位返回 -1（接口层展示 N/A）")
    void latencyWithoutSamples() {
        assertThat(metrics.latencyP50()).isEqualTo(-1L);
        assertThat(metrics.latencyP95()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("成本估算：按元/百万 token 单价")
    void costEstimation() {
        metrics.configurePrices(1.1, 4.4);
        metrics.record(1_000_000L, new DefaultUsage(1_000_000, 1_000_000));  // 各 100 万 token

        // 1.1 + 4.4 = 5.5 元
        assertThat(metrics.estimatedCostYuan()).isCloseTo(5.5, org.assertj.core.api.Assertions.within(0.0001));
        assertThat(metrics.priceConfigured()).isTrue();
    }

    @Test
    @DisplayName("单价未配置：成本 0 + priceNote 说明原因（不显示误导性数字）")
    void priceNotConfigured() {
        metrics.record(1_000_000L, new DefaultUsage(1000, 500));

        assertThat(metrics.estimatedCostYuan()).isZero();
        assertThat(metrics.priceConfigured()).isFalse();
        assertThat(metrics.snapshot().get("priceNote").toString())
                .contains("未配置模型单价");
    }

    @Test
    @DisplayName("清零：计数与延迟样本全部归零")
    void resetClearsEverything() {
        metrics.record(5_000_000L, new DefaultUsage(100, 50));
        metrics.reset();

        var snapshot = metrics.snapshot();
        assertThat(snapshot.get("callCount")).isEqualTo(0L);
        assertThat(snapshot.get("totalTokens")).isEqualTo(0L);
        assertThat(metrics.latencyP50()).isEqualTo(-1L);
    }

    @Test
    @DisplayName("环形缓冲不越界：超过容量后仍可正常统计")
    void ringBufferOverflow() {
        for (int i = 0; i < 600; i++) {   // 超过 512 容量
            metrics.record(1_000_000L, new DefaultUsage(1, 1));
        }
        assertThat(metrics.latencyP50()).isGreaterThanOrEqualTo(0);
        assertThat(metrics.snapshot().get("callCount")).isEqualTo(600L);
    }
}

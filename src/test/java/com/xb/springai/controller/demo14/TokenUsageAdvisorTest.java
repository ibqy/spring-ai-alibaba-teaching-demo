package com.xb.springai.controller.demo14;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TokenUsageAdvisor 测试 —— 验证"记账不打扰主流程"的装饰器语义
 *
 * <p>为什么值得单测：Advisor 夹在业务与模型之间，一旦写错（吞响应、改响应、
 * 把异常咽了）影响所有端点，但编译看不出来。用 mock 链路验证四条语义：
 * 正常记账、响应原样透传、失败也记延迟且异常上抛、元数据缺失不 NPE。</p>
 *
 * @author ibqy
 */
@DisplayName("TokenUsageAdvisor 计量顾问")
class TokenUsageAdvisorTest {

    private TokenUsageMetrics metrics;
    private TokenUsageAdvisor advisor;

    @BeforeEach
    void setUp() {
        metrics = new TokenUsageMetrics();
        advisor = new TokenUsageAdvisor(metrics);
    }

    /** 构造带用量的模型响应 */
    private ChatClientResponse responseWithUsage(Integer prompt, Integer completion) {
        ChatResponse chatResponse = ChatResponse.builder()
                .generations(java.util.List.of(new Generation(new AssistantMessage("回复"))))
                .metadata(ChatResponseMetadata.builder()
                        .usage(new DefaultUsage(prompt, completion))
                        .build())
                .build();
        return ChatClientResponse.builder().chatResponse(chatResponse).build();
    }

    @Test
    @DisplayName("正常调用：用量入账，响应原样返回（装饰器透明）")
    void recordsUsageAndPassesResponseThrough() {
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatClientResponse expected = responseWithUsage(120, 35);
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(expected);

        ChatClientResponse actual = advisor.adviseCall(mock(ChatClientRequest.class), chain);

        assertThat(actual).isSameAs(expected);   // 原样透传，没有包装/修改
        var snapshot = metrics.snapshot();
        assertThat(snapshot.get("callCount")).isEqualTo(1L);
        assertThat(snapshot.get("promptTokens")).isEqualTo(120L);
        assertThat(snapshot.get("completionTokens")).isEqualTo(35L);
    }

    @Test
    @DisplayName("调用失败：延迟仍入账，异常原样上抛（不咽异常）")
    void recordsLatencyOnFailureAndRethrows() {
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        when(chain.nextCall(any(ChatClientRequest.class)))
                .thenThrow(new RuntimeException("DashScope 超时"));

        assertThatThrownBy(() -> advisor.adviseCall(mock(ChatClientRequest.class), chain))
                .hasMessageContaining("DashScope 超时");

        assertThat(metrics.snapshot().get("callCount")).isEqualTo(1L);   // 失败也记一次
        assertThat(metrics.snapshot().get("totalTokens")).isEqualTo(0L); // 无用量按 0
        assertThat(metrics.latencyP50()).isGreaterThanOrEqualTo(0);      // 延迟入账
    }

    @Test
    @DisplayName("元数据缺失：usage 为 null 时不 NPE（部分代理不返回用量）")
    void nullMetadataIsSafe() {
        CallAdvisorChain chain = mock(CallAdvisorChain.class);
        ChatResponse chatResponse = ChatResponse.builder()
                .generations(java.util.List.of(new Generation(new AssistantMessage("回复"))))
                .build();   // 不设置 metadata
        ChatClientResponse response = ChatClientResponse.builder().chatResponse(chatResponse).build();
        when(chain.nextCall(any(ChatClientRequest.class))).thenReturn(response);

        ChatClientResponse actual = advisor.adviseCall(mock(ChatClientRequest.class), chain);

        assertThat(actual).isSameAs(response);
        assertThat(metrics.snapshot().get("callCount")).isEqualTo(1L);
        assertThat(metrics.snapshot().get("totalTokens")).isEqualTo(0L);
    }

    @Test
    @DisplayName("Advisor 元信息：名称与顺序（Spring AI 装配用）")
    void advisorMetadata() {
        assertThat(advisor.getName()).isEqualTo("TokenUsageAdvisor");
        assertThat(advisor.getOrder()).isEqualTo(org.springframework.core.Ordered.LOWEST_PRECEDENCE);
    }
}

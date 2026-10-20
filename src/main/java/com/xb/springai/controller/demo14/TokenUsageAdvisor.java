package com.xb.springai.controller.demo14;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.core.Ordered;

/**
 * Token 计量 Advisor —— 横切关注点收口（demo14）
 *
 * <p>作者：ibqy | 日期：2026-10-16</p>
 *
 * <p><b> Advisor 是 Spring AI 的横切扩展点</b>：像 AOP 切面，能在"调用大模型"前后插入逻辑。
 * 计量正是典型横切关注点——如果写在每个 Controller 里，demo01~demo13 每个端点都要抄一遍，
 * 以后新增端点还会漏。挂在 Advisor 上，一次装配全局生效（demo14 的 chat 端点即示范）。</p>
 *
 * <p><b>与 demo11 的关系</b>：demo11 教了"能力组合"（RAG+记忆+工具），demo14 教"生产运维"——
 * 能力上线前必须先能回答"每次调用花多少 token、多少钱、多快"。</p>
 *
 * <p><b>实现要点</b>：</p>
 * <ol>
 *   <li>先放行链路（{@code chain.nextCall(request)}）再记账——Advisor 是装饰器，不能吞响应；</li>
 *   <li>只读不改：从 {@code response.chatResponse().getMetadata().getUsage()} 取用量，原样返回响应；</li>
 *   <li>失败也记账：调用抛异常时补记一条无用量记录（延迟是排查超时的线索），异常原样抛出。</li>
 * </ol>
 *
 * @author ibqy
 */
public class TokenUsageAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(TokenUsageAdvisor.class);

    private final TokenUsageMetrics metrics;

    public TokenUsageAdvisor(TokenUsageMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        long start = System.nanoTime();
        try {
            ChatClientResponse response = chain.nextCall(request);
            // 响应元数据里的 Usage 由模型返回（DashScope 的 prompt_tokens/completion_tokens）
            var usage = response.chatResponse() != null
                    && response.chatResponse().getMetadata() != null
                    ? response.chatResponse().getMetadata().getUsage()
                    : null;
            metrics.record(System.nanoTime() - start, usage);
            log.debug("[demo14] 记账完成 prompt={} completion={}",
                    usage != null ? usage.getPromptTokens() : null,
                    usage != null ? usage.getCompletionTokens() : null);
            return response;
        } catch (RuntimeException e) {
            // 失败的调用也要记延迟（usage 传 null，计量器内部按 0 计）
            metrics.record(System.nanoTime() - start, null);
            throw e;
        }
    }

    @Override
    public String getName() {
        return "TokenUsageAdvisor";
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}

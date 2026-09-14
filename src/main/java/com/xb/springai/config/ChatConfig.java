package com.xb.springai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局 Bean 配置 —— 提供可复用的依赖
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>为什么需要它</b>：<code>ChatMemory</code> 接口没有一个开箱即用的单例 Bean，
 * 因此我们在启动时手动声明一个"内存版窗口记忆"（Spring AI 1.x 的标准内存实现）。
 * 这样 demo06（会话记忆）等地方就能直接依赖注入使用。</p>
 *
 * <p><b>说明</b>：这里用的是<b>进程内内存</b>，重启即清空，适合教学；
 * 生产环境可换成 Redis/数据库实现。</p>
 */
@Configuration
public class ChatConfig {

    /**
     * 声明一个全局单例的 ChatMemory。
     *
     * <p>MessageWindowChatMemory 基于 InMemoryChatMemoryRepository，
     * maxMessages(20) 表示每个会话最多保留最近 20 条消息。</p>
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)      // 单个会话最多记住最近 20 条
                .build();
    }
}
package com.xb.springai.controller.demo02;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * demo02：流式对话 —— 让 AI 一个字一个字"打字"给你看
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>对比 demo01</b>：demo01 的 <code>call()</code> 是<b>一次性</b>等完整结果；
 * 这里用 <code>stream()</code> 实现<b>逐字输出</b>（Server-Sent Events）。
 * 用户体验上"出字快、不干等"，是聊天类应用的标准做法。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo02/chat-stream?message=写一首关于春天的短诗</code>，
 * 浏览器会看到文字流式滚动输出。</p>
 */
@RestController
@RequestMapping("/api/demo02")
public class Demo02StreamingChatController {

    private final ChatClient chatClient;

    public Demo02StreamingChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个中文诗人，擅长写短文与诗歌。")
                .build();
    }

    /**
     * 流式对话：返回 Flux&lt;String&gt;，Spring 会以 text/event-stream 逐块推送。
     */
    @GetMapping(value = "/chat-stream", produces = "text/event-stream;charset=UTF-8")
    public Flux<String> chatStream(@RequestParam(defaultValue = "写一首关于春天的短诗") String message) {
        return chatClient.prompt(message)
                .stream()          // 关键：与 demo01 的 call() 不同，这里是流式
                .content();        // 返回 Flux<String>：模型的文字天然带有换行，逐块推给前端
    }
}
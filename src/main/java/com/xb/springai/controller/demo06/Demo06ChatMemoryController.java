package com.xb.springai.controller.demo06;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo06：会话记忆 —— 让 AI"记住"我们聊过什么
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>痛点</b>：每次调用默认是"失忆"的，上次说的话下次就忘了。
 * <b>解决</b>：用 {@link MessageChatMemoryAdvisor} 把对话历史存进 {@link ChatMemory}，
 * 下次提问自动带着上下文。通过自定义 <code>conversationId</code> 可以让不同会话彼此隔离。</p>
 *
 * <p><b>运行</b>（注意记忆效果需要连发两句）：
 * <br/>1. <code>GET /api/demo06/chat?conversationId=xb&message=我的名字是小北</code>
 * <br/>2. <code>GET /api/demo06/chat?conversationId=xb&message=我叫什么？</code> ← 它会答"小北"</p>
 */
@RestController
@RequestMapping("/api/demo06")
public class Demo06ChatMemoryController {

    private final ChatClient chatClient;

    /**
     * 构造时挂上记忆顾问。这里建一个可注入的单例 ChatMemory（内存实现）。
     */
    public Demo06ChatMemoryController(ChatClient.Builder chatClientBuilder,
                                      ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        // 把"记忆"挂进去；chatMemory 是上面构造的全局单例，可跨请求复用
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @GetMapping("/chat")
    public String chat(
            @RequestParam(defaultValue = "xb") String conversationId, // 会话 ID：相同 ID 共享记忆
            @RequestParam(defaultValue = "你好") String message) {
        return chatClient.prompt()
                // 指定本次使用的会话 ID，供记忆顾问检索对应历史（1.0.0 用该上下文 key）
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .user(message)
                .call()
                .content();
    }
}
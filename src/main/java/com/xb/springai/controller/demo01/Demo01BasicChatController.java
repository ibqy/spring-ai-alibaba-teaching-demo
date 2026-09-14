package com.xb.springai.controller.demo01;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo01：第一个聊天接口 —— 认识 ChatClient
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>要点</b>：注入 {@link ChatClient}（由 Spring AI Alibaba 自动装配），
 * 通过 <code>prompt(...).call().content()</code> 就能让通义千问回复。</p>
 *
 * <p><b>运行</b>：启动后访问
 * <code>GET /api/demo01/chat?message=你好</code></p>
 */
@RestController
@RequestMapping("/api/demo01")
public class Demo01BasicChatController {

    private final ChatClient chatClient;

    /**
     * 构造注入 ChatClient。Builder 由 starter 自动配置，我们也给默认人设。
     */
    public Demo01BasicChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // 给每次对话设定一个默认"角色人设"，未指定 system 时都会带上
                .defaultSystem("你是一个博学、友好的中文智能助手。")
                .build();
    }

    /**
     * 最简单的同步对话：一问一答，返回纯文本。
     */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你好，介绍一下你自己") String message) {
        // prompt() 接收用户消息，call() 同步调用模型，content() 取出文本回答
        return chatClient.prompt(message).call().content();
    }

    /**
     * 进阶：返回完整的 ChatResponse，能看到 token 消耗等元信息。
     */
    @GetMapping("/chat-full")
    public String chatFull(@RequestParam(defaultValue = "你好") String message) {
        ChatResponse response = chatClient.prompt(message).call().chatResponse();
        if (response == null || response.getResult() == null) {
            return "模型没有返回结果。请检查 DASHSCOPE_API_KEY 是否配置正确。";
        }
        // 取文本回答：Spring AI 1.x 中 AssistantMessage 用 getText() 取纯文本
        return "回答: " + response.getResult().getOutput().getText()
                + "\n\n[元信息] finishReason=" + response.getResult().getMetadata().getFinishReason();
    }
}
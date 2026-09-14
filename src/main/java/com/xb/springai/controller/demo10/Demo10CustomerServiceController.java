package com.xb.springai.controller.demo10;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo10：组合实战 · 一个能用的多轮智能客服
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>这个 demo 把前面学的能力"组装"成一个完整的客服机器人</b>：</p>
 * <ul>
 *   <li><b>人设</b>：defaultSystem 设定成"小北"，限定语气与边界</li>
 *   <li><b>记忆</b>：MessageChatMemoryAdvisor 记住顾客聊过什么（多轮）</li>
 *   <li><b>工具</b>：售后政策工具，让它能查退换货规则</li>
 *   <li><b>联网</b>：DashScope 特有 enableSearch，回答时事类提问</li>
 * </ul>
 *
 * <p><b>运行</b>：POST /api/demo10/ask（body 为 JSON）</p>
 */
@RestController
@RequestMapping("/api/demo10")
public class Demo10CustomerServiceController {

    private final ChatClient chatClient;

    public Demo10CustomerServiceController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                // ① 人设
                .defaultSystem("你是'小北咖啡店'的智能客服'小北'。语气友好简洁，"
                        + "遇到售后问题查售后政策工具，根据查到的规则回答，不要编造。")
                // ② 记忆：多轮上下文
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                // ③ 工具：售后政策查询
                .defaultTools(new AfterSalesTool())
                .build();
    }

    @PostMapping("/ask")
    public String ask(@RequestBody Demo10AskRequest req) {
        String conversationId = req.conversationId() == null || req.conversationId().isBlank()
                ? "default" : req.conversationId();

        return chatClient.prompt()
                // 记忆的会话隔离 key（Spring AI 1.0.0）
                .advisors(a -> a.param("chat_memory_conversation_id", conversationId))
                .user(req.message())
                .call()
                .content();
    }

    /**
     * 附带联网搜索的客服回答（用于"今天天气""最近新闻"之类实时问题）。
     */
    @PostMapping("/ask-with-search")
    public String askWithSearch(@RequestBody Demo10AskRequest req) {
        return chatClient.prompt(req.message())
                .options(DashScopeChatOptions.builder()
                        .withEnableSearch(true)   // 开启联网搜索（DashScope 特色）
                        .build())
                .call()
                .content();
    }
}
package com.xb.springai.controller.demo07;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo07：系统角色 + 阿里云商业化能力 —— 设定人设，并开启网络搜索
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>第一部分 · 系统角色</b>：<code>defaultSystem(...)</code> 设定 AI 的"人设/边界"，
 * 用一句话就能限定它的语气、身份和回答范围。</p>
 *
 * <p><b>第二部分 · 联网搜索（DashScope 特色）</b>：通过
 * {@link DashScopeChatOptions#builder()} 的 <code>withEnableSearch(true)</code>，
 * 让 <b>qwen</b> 实时联网检索，能回答训练数据截止之后的新事件。
 * 这是官方 Spring AI 所没有的、通义特有的实用能力。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo07/search?q=2026年奥运会举办地</code></p>
 */
@RestController
@RequestMapping("/api/demo07")
public class Demo07SystemRoleController {

    private final ChatClient chatClient;

    public Demo07SystemRoleController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                // 设定人设：一个乐于助人的小编剧
                .defaultSystem("你是一位博学、幽默的中文科普作者，回答简洁、准确、生动。")
                .build();
    }

    /**
     * 开启联网搜索：通过 .options(DashScopeChatOptions...) 覆盖本次调用的参数。
     */
    @GetMapping("/search")
    public String search(@RequestParam(defaultValue = "2026年冬奥会在哪里举办") String q) {
        return chatClient.prompt(q)
                // 关键：DashScope 专有能力，让模型联网实时检索后再回答
                .options(DashScopeChatOptions.builder()
                        .withModel("qwen-plus")     // 指定模型
                        .withEnableSearch(true)     // 开启网络搜索
                        .build())
                .call()
                .content();
    }

    /**
     * 纯人设演示：只用来控制"说话方式"。
     */
    @GetMapping("/advice")
    public String advice(
            @RequestParam(defaultValue = "什么是微服务") String topic,
            @RequestParam(defaultValue = "初学者") String audience) {
        // 动态覆盖系统角色：用 prompt().system(...) 覆盖默认人设
        return chatClient.prompt()
                .system("你现在是一个给「" + audience + "」讲课的老师，请用打比方的方式讲清楚。")
                .user(topic)
                .call()
                .content();
    }
}
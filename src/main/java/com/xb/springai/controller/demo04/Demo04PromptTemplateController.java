package com.xb.springai.controller.demo04;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo04：提示词模板 —— 复用同一套话术，只换变量
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>类比 JDBC</b>：把提示词写成一串带 <code>{占位符}</code> 的模板，
 * 每次传入不同变量即可产出不同的提问，避免重复拼接字符串。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo04/poem?topic=春天&style=婉约</code></p>
 */
@RestController
@RequestMapping("/api/demo04")
public class Demo04PromptTemplateController {

    private final ChatClient chatClient;

    public Demo04PromptTemplateController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 用 {变量} 占位符组织提示词。
     */
    @GetMapping("/poem")
    public String poem(
            @RequestParam(defaultValue = "春天") String topic,
            @RequestParam(defaultValue = "浪漫") String style) {

        // 模板中的 {topic}、{style} 会被下面的变量自动替换
        return chatClient.prompt()
                .user(u -> u
                        .text("请围绕「{topic}」写一首风格为「{style}」的四句绝句。"
                                + "只输出诗本身，不要解释。")
                        .param("topic", topic)   // 绑定变量
                        .param("style", style))  // 绑定变量
                .call()
                .content();
    }
}
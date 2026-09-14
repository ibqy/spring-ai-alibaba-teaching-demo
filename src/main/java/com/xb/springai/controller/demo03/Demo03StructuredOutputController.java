package com.xb.springai.controller.demo03;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo03：结构化输出 —— 让 AI 返回"能直接当 Java 对象用"的 JSON
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>问题</b>：直接问大模型返回的是自由文本，没法塞进业务系统。
 * <b>答案</b>：用 <code>entity(ActorFilm.class)</code>，Spring AI 会要求模型
 * 只输出符合该结构（字段）的 JSON，并自动反序列化成对象。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo03/actor?actor=周星驰</code></p>
 */
@RestController
@RequestMapping("/api/demo03")
public class Demo03StructuredOutputController {

    private final ChatClient chatClient;

    public Demo03StructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 让模型返回一个 ActorFilm 对象。
     */
    @GetMapping("/actor")
    public ActorFilm actor(@RequestParam(defaultValue = "周星驰") String actor) {
        // 明确告诉模型字段含义，提高输出准确性；call().entity(ActorFilm.class) 完成映射
        return chatClient.prompt()
                .user(String.format("请介绍演员 %s，只输出一个严格的 JSON 对象，字段：name(姓名)、"
                        + "genre(代表作类型)、famousWork(代表作)、reason(推荐理由)。不要输出其它内容。", actor))
                .call()
                .entity(ActorFilm.class);   // 关键：JSON -> Java record
    }
}
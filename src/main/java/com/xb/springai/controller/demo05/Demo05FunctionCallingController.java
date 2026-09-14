package com.xb.springai.controller.demo05;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * demo05：函数调用（Function Calling）—— 让模型"用工具查实时数据"
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>核心思想</b>：把 {@link WeatherTools} 通过 <code>tools(...)</code> 挂到一次调用上。
 * 当用户提问需要实时信息时，模型自动决定"该调用 get_weather 工具"，拿到结果后再组织语言。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo05/weather?city=杭州</code></p>
 */
@RestController
@RequestMapping("/api/demo05")
public class Demo05FunctionCallingController {

    private final ChatClient chatClient;

    /**
     * 把工具对象注册到 ChatClient，之后每次 prompt 都会携带这套工具。
     */
    public Demo05FunctionCallingController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultTools(new WeatherTools())   // 关键：把工具挂进来
                .build();
    }

    @GetMapping("/weather")
    public String weather(@RequestParam(defaultValue = "杭州") String city) {
        return chatClient.prompt("今天" + city + "的天气怎么样？适合出门吗？请简单回答。")
                .call()
                .content();
    }
}
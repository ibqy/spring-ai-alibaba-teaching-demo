package com.xb.springai.controller.demo13;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * demo13：A2A（Agent-to-Agent）—— 智能体间通信协议
 *
 * <p>作者：ibqy | 日期：2026-09-22</p>
 *
 * <p><b>教学知识点</b>：A2A 是 Google 提出的智能体间通信标准，
 * 让不同框架构建的 Agent 能互相发现和调用。本 Demo 演示如何让
 * Spring AI Alibaba Agent 暴露标准 A2A 端点。</p>
 *
 * <p><b>核心概念</b>：</p>
 * <ul>
 *     <li><b>AgentCard</b>：Agent 的"名片"，包含名称、技能、端点</li>
 *     <li><b>JSON-RPC</b>：A2A 使用 JSON-RPC 2.0 通信</li>
 * </ul>
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api/demo13")
public class Demo13A2aController {

    private final ChatClient chatClient;
    private final boolean a2aEnabled;

    @Value("${server.port:8080}")
    private int serverPort;

    public Demo13A2aController(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.a2a.server.enabled:false}") boolean a2aEnabled) {
        this.a2aEnabled = a2aEnabled;
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一个支持 A2A 协议的 Spring AI Alibaba 智能助手。\n"
                        + "基于通义千问 DashScope 模型，你可以被其他 Agent 通过 A2A 协议发现和调用。\n"
                        + "请用中文简洁专业地回答问题。")
                .build();
    }

    /**
     * GET /api/demo13/ask?message=你好
     */
    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "你好，请介绍一下你的能力") String message) {
        return this.chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * GET /api/demo13/status —— 查看 A2A 状态。
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("a2aEnabled", a2aEnabled);
        result.put("agentName", "spring-ai-alibaba-agent");
        result.put("agentDescription", "基于通义千问的 Spring AI Alibaba 教学 Agent");
        result.put("agentCardUrl", "http://localhost:" + serverPort + "/a2a/card");
        result.put("jsonRpcUrl", "http://localhost:" + serverPort + "/a2a/");
        if (a2aEnabled) {
            result.put("skills", new String[]{"通用对话", "知识问答"});
            result.put("hint", "A2A 已启用，其他 Agent 可通过 /a2a/card 发现本 Agent");
        } else {
            result.put("hint", "设置环境变量 A2A_ENABLED=true 并重启即可启用 A2A");
        }
        return result;
    }
}

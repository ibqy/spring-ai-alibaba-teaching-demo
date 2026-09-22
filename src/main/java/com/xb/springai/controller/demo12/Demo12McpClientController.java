package com.xb.springai.controller.demo12;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * demo12：MCP 客户端 —— 通过 Model Context Protocol 接入外部工具服务
 *
 * <p>作者：ibqy | 日期：2026-09-22</p>
 *
 * <p><b>教学知识点</b>：MCP（Model Context Protocol）是 Anthropic 提出的
 * "AI 应用与外部工具之间的 USB-C 标准"。本 Demo 演示 Spring AI Alibaba
 * 作为 MCP 客户端连接外部 MCP 服务器，让模型自动调用 MCP 工具。</p>
 *
 * <p><b>工作原理</b>：</p>
 * <ol>
 *     <li>application.yml 配置 stdio 服务器（如文件系统 MCP Server）</li>
 *     <li>Spring AI 自动配置将 MCP 工具打包为 {@link ToolCallbackProvider}</li>
 *     <li>ChatClient 挂载该 Provider，模型按需调用 MCP 工具</li>
 * </ol>
 *
 * <p><b>启动前准备</b>（默认关闭）：</p>
 * <pre>
 * # 1) 安装 Node.js（npx 随附）
 * # 2) 设置环境变量：
 * #    export MCP_ENABLED=true
 * # 3) 访问 /api/demo12/status 确认 MCP 工具已就绪
 * </pre>
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api/demo12")
public class Demo12McpClientController {

    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpToolProvider;

    public Demo12McpClientController(
            ChatClient.Builder chatClientBuilder,
            @Autowired(required = false) ToolCallbackProvider mcpToolCallbackProvider) {
        this.mcpToolProvider = mcpToolCallbackProvider;

        if (mcpToolCallbackProvider != null) {
            this.chatClient = chatClientBuilder
                    .defaultToolCallbacks(mcpToolCallbackProvider)
                    .defaultSystem("你是一个接入了 MCP 工具服务的智能助手。\n"
                            + "当用户询问文件内容、目录结构或需要读写文件时，优先调用 MCP 工具完成，\n"
                            + "并将工具返回的真实结果整理成中文回答。")
                    .build();
        } else {
            this.chatClient = chatClientBuilder
                    .defaultSystem("当前未接入 MCP 服务器（MCP_ENABLED=false）。")
                    .build();
        }
    }

    /**
     * GET /api/demo12/ask?question=查看当前项目目录下有哪些文件
     */
    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "查看当前项目目录下有哪些文件") String question) {
        return this.chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * GET /api/demo12/status —— 查看 MCP 连接状态。
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mcpEnabled", mcpToolProvider != null);
        if (mcpToolProvider != null) {
            var callbacks = mcpToolProvider.getToolCallbacks();
            result.put("toolCount", callbacks.length);
            result.put("tools", java.util.Arrays.stream(callbacks)
                    .map(tc -> Map.of("name", tc.getToolDefinition().name(),
                            "description", tc.getToolDefinition().description()))
                    .toList());
        } else {
            result.put("hint", "设置环境变量 MCP_ENABLED=true 并重启即可启用 MCP");
        }
        return result;
    }
}

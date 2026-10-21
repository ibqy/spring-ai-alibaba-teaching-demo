# Demo 总览

基于 **Spring AI Alibaba 1.0.0.2 + DashScope 通义千问**，演示 14 个由浅入深的 AI 教学示例。

## 技术栈

- Java 21 · Spring Boot 3.x
- Spring AI Alibaba 1.0.0.2（阿里对 Spring AI 的通义生态适配层）
- DashScope API（通义千问系列模型）

## 14 个 Demo 一览

| Demo | 知识点 | 接口 | 代码 |
|------|--------|------|------|
| demo01 | 基础对话 ChatClient | `GET /api/demo01/chat`、`/chat-full` | [Demo01BasicChatController](../src/main/java/com/xb/springai/controller/demo01/Demo01BasicChatController.java) |
| demo02 | 流式对话 Streaming | `GET /api/demo02/chat-stream`（SSE） | [Demo02StreamingChatController](../src/main/java/com/xb/springai/controller/demo02/Demo02StreamingChatController.java) |
| demo03 | 结构化输出 POJO | `GET /api/demo03/actor` | [Demo03StructuredOutputController](../src/main/java/com/xb/springai/controller/demo03/Demo03StructuredOutputController.java) |
| demo04 | 提示词模板 | `GET /api/demo04/poem` | [Demo04PromptTemplateController](../src/main/java/com/xb/springai/controller/demo04/Demo04PromptTemplateController.java) |
| demo05 | 函数调用 Function Calling | `GET /api/demo05/weather` | [Demo05FunctionCallingController](../src/main/java/com/xb/springai/controller/demo05/Demo05FunctionCallingController.java) · [WeatherTools](../src/main/java/com/xb/springai/controller/demo05/WeatherTools.java) |
| demo06 | 会话记忆 ChatMemory | `GET /api/demo06/chat` | [Demo06ChatMemoryController](../src/main/java/com/xb/springai/controller/demo06/Demo06ChatMemoryController.java) |
| demo07 | 系统角色 + 联网搜索 | `GET /api/demo07/search`、`/advice` | [Demo07SystemRoleController](../src/main/java/com/xb/springai/controller/demo07/Demo07SystemRoleController.java) |
| demo08 | RAG 检索增强 | `GET /api/demo08/ask` | [Demo08RagController](../src/main/java/com/xb/springai/controller/demo08/Demo08RagController.java) |
| demo09 | 列表结构化输出 | `GET /api/demo09/orders`、`/orders-schema` | [Demo09ListStructuredOutputController](../src/main/java/com/xb/springai/controller/demo09/Demo09ListStructuredOutputController.java) |
| demo10 | 综合客服（记忆 + 工具） | `POST /api/demo10/ask`、`/ask-with-search` | [Demo10CustomerServiceController](../src/main/java/com/xb/springai/controller/demo10/Demo10CustomerServiceController.java) · [AfterSalesTool](../src/main/java/com/xb/springai/controller/demo10/AfterSalesTool.java) |
| demo11 | 高级 Agent（RAG + 工具 + 记忆 + 流式） | `POST /api/demo11/ask`、`GET /api/demo11/stream` | [Demo11AdvancedAgentController](../src/main/java/com/xb/springai/controller/demo11/Demo11AdvancedAgentController.java) · [ShopServiceTool](../src/main/java/com/xb/springai/controller/demo11/ShopServiceTool.java) |
| demo12 | MCP 客户端 | `GET /api/demo12/ask`、`/status` | [Demo12McpClientController](../src/main/java/com/xb/springai/controller/demo12/Demo12McpClientController.java) |
| demo13 | A2A 智能体互调 | `GET /api/demo13/ask`、`/status` | [Demo13A2aController](../src/main/java/com/xb/springai/controller/demo13/Demo13A2aController.java) |
| demo14 | Token 用量可观测 | `GET /api/demo14/chat`、`/usage` | [Demo14TokenObservabilityController](../src/main/java/com/xb/springai/controller/demo14/Demo14TokenObservabilityController.java) · [TokenUsageMetrics](../src/main/java/com/xb/springai/controller/demo14/TokenUsageMetrics.java)

## 建议学习路径

1. **基础入门（demo01–04）**：先跑通对话与流式输出，理解 ChatClient、结构化输出和提示词模板三件套
2. **核心能力（demo05–08）**：函数调用让模型操作真实业务，会话记忆与系统角色构成多轮应用的地基，RAG 让模型回答私有知识
3. **综合实战（demo09–11）**：列表输出处理批量结果，demo10/demo11 把记忆、RAG、工具调用组合成一个真实可用的客服应用
4. **协议与可观测（demo12–14）**：MCP 接外部工具、A2A 与其他 Agent 互通、Token 计量把成本看住——这一步决定 Demo 能否上线

## 快速开始

```bash
export DASHSCOPE_API_KEY=sk-xxx
cd spring-ai-alibaba-teaching-demo
mvn spring-boot:run
```

DashScope API Key 在[阿里云百炼控制台](https://bailian.console.aliyun.com/)申请。

## 详细教学指南

图文版完整教程见仓库 [docs/spring-ai-alibaba-teaching-guide/](https://github.com/ibqy/spring-ai-alibaba-teaching-demo/tree/main/docs/spring-ai-alibaba-teaching-guide) 目录（HTML 版，可直接在浏览器打开）。

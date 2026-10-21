---
layout: home

hero:
  name: Spring AI Alibaba 教学
  text: 通义千问生态教学项目
  tagline: 14 个 Demo 玩转 Spring AI Alibaba + DashScope 通义千问：对话、RAG、工具调用、MCP、A2A，到综合客服与 Token 计量
  actions:
    - theme: brand
      text: Demo 总览 →
      link: /01-Demo总览
    - theme: alt
      text: GitHub 源码
      link: https://github.com/ibqy/spring-ai-alibaba-teaching-demo

features:
  - icon: 🌊
    title: 通义千问接入
    details: DashScope API 对接、流式输出、对话记忆的基础用法
  - icon: 📝
    title: 结构化输出
    details: 大模型输出映射为 Java 对象，告别字符串解析
  - icon: 📚
    title: RAG 检索增强
    details: 文档向量化与知识库问答的最小实现
  - icon: 🛠️
    title: 工具调用
    details: Function Calling 让模型操作真实业务能力
  - icon: 🤖
    title: MCP 与 A2A
    details: MCP 接入外部工具，A2A 实现 Agent 间通信，两大协议全覆盖
  - icon: 🎧
    title: Demo 10 · 综合客服
    details: 意图识别 + 工具调用 + 记忆的组合式客服应用
  - icon: 🧠
    title: Demo 11 · 知识型客服
    details: RAG + 客服场景落地，知识库驱动的精准回答
---

## 课程结构

| 章节 | 主题 | 对应代码 |
|------|------|----------|
| [01 · Demo 总览](/01-Demo总览) | 技术栈、Demo 索引、学习路径 | 14 个 `controller/demoXX` |
| [02 · 基础能力](/02-基础能力) | demo01–04：对话、流式、结构化输出、提示词模板 | [`demo01`](../src/main/java/com/xb/springai/controller/demo01) – [`demo04`](../src/main/java/com/xb/springai/controller/demo04) |
| [03 · 核心进阶](/03-核心进阶) | demo05–08：函数调用、会话记忆、系统角色、RAG | [`demo05`](../src/main/java/com/xb/springai/controller/demo05) – [`demo08`](../src/main/java/com/xb/springai/controller/demo08) |
| [04 · 综合实战](/04-综合实战) | demo09–11：列表输出、综合客服、高级 Agent | [`demo09`](../src/main/java/com/xb/springai/controller/demo09) – [`demo11`](../src/main/java/com/xb/springai/controller/demo11) |
| [05 · MCP 与 A2A](/05-mcp-a2a) | demo12–13：接外部工具、与其他 Agent 互通 | [`demo12`](../src/main/java/com/xb/springai/controller/demo12) / [`demo13`](../src/main/java/com/xb/springai/controller/demo13) |
| [06 · Token 用量可观测](/06-token-observability) | demo14：Advisor 横切计量、延迟分位、成本估算 | [`demo14`](../src/main/java/com/xb/springai/controller/demo14) |

## 代码地图

| 包 | 关键类 | 职责 |
|----|--------|------|
| 根包 | [SpringAiAlibabaTeachingDemoApplication](../src/main/java/com/xb/springai/SpringAiAlibabaTeachingDemoApplication.java) | 启动入口 |
| `config` | [ChatConfig](../src/main/java/com/xb/springai/config/ChatConfig.java) / [RagConfig](../src/main/java/com/xb/springai/config/RagConfig.java) | 全局 `ChatMemory` 单例（窗口 20 条）；启动时把 `kb/shop-intro.txt` 灌进 `SimpleVectorStore` |
| `common` | [ApiResponse](../src/main/java/com/xb/springai/common/ApiResponse.java) / [GlobalExceptionHandler](../src/main/java/com/xb/springai/common/GlobalExceptionHandler.java) | 统一响应包装与异常兜底，避免模型异常直接冒到 HTTP 层 |
| `controller/demo01`–`demo04` | [Demo01BasicChatController](../src/main/java/com/xb/springai/controller/demo01/Demo01BasicChatController.java) / [Demo02StreamingChatController](../src/main/java/com/xb/springai/controller/demo02/Demo02StreamingChatController.java) / [Demo03StructuredOutputController](../src/main/java/com/xb/springai/controller/demo03/Demo03StructuredOutputController.java) / [Demo04PromptTemplateController](../src/main/java/com/xb/springai/controller/demo04/Demo04PromptTemplateController.java) | ChatClient 四件套：同步调用、SSE 流式、`outputType` 映射 POJO、提示词模板 |
| `controller/demo05`–`demo08` | [Demo05FunctionCallingController](../src/main/java/com/xb/springai/controller/demo05/Demo05FunctionCallingController.java) / [Demo06ChatMemoryController](../src/main/java/com/xb/springai/controller/demo06/Demo06ChatMemoryController.java) / [Demo07SystemRoleController](../src/main/java/com/xb/springai/controller/demo07/Demo07SystemRoleController.java) / [Demo08RagController](../src/main/java/com/xb/springai/controller/demo08/Demo08RagController.java) | 工具调用（`@Tool`）、按 conversationId 隔离的记忆、系统角色 + 联网搜索、`similaritySearch` 检索 |
| `controller/demo09`–`demo11` | [Demo09ListStructuredOutputController](../src/main/java/com/xb/springai/controller/demo09/Demo09ListStructuredOutputController.java) / [Demo10CustomerServiceController](../src/main/java/com/xb/springai/controller/demo10/Demo10CustomerServiceController.java) / [Demo11AdvancedAgentController](../src/main/java/com/xb/springai/controller/demo11/Demo11AdvancedAgentController.java) | 列表提取（`outputType` vs `outputSchema`）、售后客服、RAG + 工具 + 记忆的能力组合 |
| `controller/demo12`–`demo13` | [Demo12McpClientController](../src/main/java/com/xb/springai/controller/demo12/Demo12McpClientController.java) / [Demo13A2aController](../src/main/java/com/xb/springai/controller/demo13/Demo13A2aController.java) | MCP 客户端（开关控制、`/status` 自查）、A2A AgentCard 与 JSON-RPC |
| `controller/demo14` | [TokenUsageMetrics](../src/main/java/com/xb/springai/controller/demo14/TokenUsageMetrics.java) / [TokenUsageAdvisor](../src/main/java/com/xb/springai/controller/demo14/TokenUsageAdvisor.java) | 纯逻辑计量核心（LongAdder + 环形缓冲分位 + 成本估算）与只读透明 Advisor |

## 在路线中的位置

> **Java × AI 进阶路线** · 阶段 2「AI 应用工程入门」第 2 站  
> **Spring AI Alibaba（通义千问生态）** → 上一站：[Spring AI 主线](https://ibqy.github.io/spring-ai-teaching-demo/) · 下一站：[Semantic Kernel for Java](https://ibqy.github.io/semantic-kernel-java-teaching-demo/)  
> 建议与上一站对照学习：同一套能力，两种抽象  
> 完整路线：[ibqy.github.io/curriculum](https://ibqy.github.io/curriculum)

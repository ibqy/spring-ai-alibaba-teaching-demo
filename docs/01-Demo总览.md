# Demo 总览

基于 **Spring AI Alibaba 1.0.0.2 + DashScope 通义千问**，演示 11 个由浅入深的 AI 教学示例。

## 技术栈

- Java 21 · Spring Boot 3.x
- Spring AI Alibaba 1.0.0.2（阿里对 Spring AI 的通义生态适配层）
- DashScope API（通义千问系列模型）

## 11 个 Demo 一览

| Demo | 知识点 | 接口 |
|------|--------|------|
| demo01 | 基础对话 ChatClient | `GET /api/demo01/chat` |
| demo02 | 流式对话 Streaming | `GET /api/demo02/stream` |
| demo03 | 结构化输出 POJO | `GET /api/demo03/structured` |
| demo04 | 提示词模板 | `GET /api/demo04/template` |
| demo05 | 函数调用 Function Calling | `GET /api/demo05/weather` |
| demo06 | 会话记忆 ChatMemory | `POST /api/demo06/chat` |
| demo07 | 系统角色 System Role | `POST /api/demo07/chat` |
| demo08 | RAG 检索增强 | `GET /api/demo08/ask` |
| demo09 | 列表输出 | `GET /api/demo09/list` |
| demo10 | 综合客服（记忆 + RAG + 工具） | `POST /api/demo10/ask` |
| demo11 | 知识型客服（深阶） | `POST /api/demo11/ask` |

## 建议学习路径

1. **基础入门（demo01–04）**：先跑通对话与流式输出，理解 ChatClient、结构化输出和提示词模板三件套
2. **核心能力（demo05–07）**：函数调用让模型操作真实业务，会话记忆与系统角色构成多轮应用的地基
3. **RAG 与输出（demo08–09）**：检索增强让模型回答私有知识，列表输出处理批量结果
4. **综合实战（demo10–11）**：把记忆、RAG、工具调用组合成一个真实可用的客服应用——demo11 是全课程的收官之作

## 快速开始

```bash
export DASHSCOPE_API_KEY=sk-xxx
cd spring-ai-alibaba-teaching-demo
mvn spring-boot:run
```

DashScope API Key 在[阿里云百炼控制台](https://bailian.console.aliyun.com/)申请。

## 详细教学指南

图文版完整教程见仓库 [docs/spring-ai-alibaba-teaching-guide/](https://github.com/ibqy/spring-ai-alibaba-teaching-demo/tree/main/docs/spring-ai-alibaba-teaching-guide) 目录（HTML 版，可直接在浏览器打开）。

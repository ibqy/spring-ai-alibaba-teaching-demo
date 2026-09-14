# Spring AI Alibaba 教学 Demo

> 作者：xb ｜ 日期：2026-09-10

基于 Spring AI Alibaba 1.0.0.2 + DashScope 通义千问，演示 11 个由浅入深的 AI 教学示例。

## Demo 一览

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
| demo10 | 综合客服（记忆+RAG+工具） | `POST /api/demo10/ask` |
| demo11 | 知识型客服（深阶） | `POST /api/demo11/ask` |

## 快速开始

```bash
export DASHSCOPE_API_KEY=sk-xxx
cd spring-ai-alibaba-teaching-demo
mvn spring-boot:run
```

详细教学指南 → [docs/](docs/spring-ai-alibaba-teaching-guide/)
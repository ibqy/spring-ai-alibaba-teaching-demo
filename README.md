<p align="center">
  <a href="https://ibqy.github.io/spring-ai-alibaba-teaching-demo/"><img src="https://img.shields.io/badge/在线课件-2F6FEB?style=flat-square" alt="在线课件"></a>
  <img src="https://img.shields.io/badge/Java-21-2266EE?style=flat-square" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?style=flat-square" alt="Spring Boot 3.5.16">
  <img src="https://img.shields.io/badge/Spring_AI-1.1.8-6DB33F?style=flat-square" alt="Spring AI 1.1.8">
  <img src="https://img.shields.io/badge/Spring_AI_Alibaba-1.1.2.4-6DB33F?style=flat-square" alt="Spring AI Alibaba 1.1.2.4">
  <img src="https://img.shields.io/github/license/ibqy/spring-ai-alibaba-teaching-demo?style=flat-square" alt="License">
  <img src="https://img.shields.io/github/stars/ibqy/spring-ai-alibaba-teaching-demo?style=flat-square" alt="Stars">
</p>

> **课程位置** 阶段 2 · AI 应用工程入门 — 第 2 站
> [上一站 Spring AI 主线](https://ibqy.github.io/spring-ai-teaching-demo/) → **Spring AI Alibaba** → [下一站 Semantic Kernel](https://ibqy.github.io/semantic-kernel-java-teaching-demo/) ｜ [完整学习路线](https://ibqy.github.io/curriculum)

---

# Spring AI Alibaba 教学 Demo

> 作者：xb ｜ 日期：2026-09-10（版本基线更新于 2026-10-16）

基于 Spring AI Alibaba 1.1.2.4-security-fix + DashScope 通义千问（Spring Boot 3.5.16 + Spring AI 1.1.8），演示 14 个由浅入深的 AI 教学示例。

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
| demo12 | MCP 客户端 | `GET /api/demo12/ask` |
| demo13 | A2A 智能体通信 | `GET /api/demo13/ask` |
| demo14 | Token 用量可观测 | `GET /api/demo14/chat` + `GET /api/demo14/usage`（Advisor 自动计量 + 成本估算） |

## 快速开始

```bash
export DASHSCOPE_API_KEY=sk-xxx
cd spring-ai-alibaba-teaching-demo
mvn spring-boot:run
```

详细教学指南 → [VitePress 文档站](docs/) · [HTML 图文版](docs/spring-ai-alibaba-teaching-guide/)

## 实现边界

### 已实现（可直接运行）

| 功能 | 说明 |
|------|------|
| 14 个渐进式 Demo | 基础对话 → 流式 → 结构化输出 → 工具调用 → RAG → 综合客服 → MCP → A2A → 用量可观测 |
| Function Calling | 天气查询 / 售后政策 / 订餐服务 3 类工具 |
| RAG 检索增强 | SimpleVectorStore + 知识库文档 |
| 会话记忆 | MessageWindowChatMemory（20 条窗口） |
| DashScope 联网搜索 | `DashScopeChatOptions.withEnableSearch(true)` |
| 流式 SSE 输出 | `Flux<String>` + `text/event-stream` |
| 全局异常处理 | `@ControllerAdvice` + `ApiResponse` 统一响应包装 |
| 参数校验 | Jakarta Validation（`@NotBlank`）+ `@Valid` |
| MCP 客户端 | demo12 接入 Model Context Protocol 外部工具 |
| A2A 服务端 | demo13 接入 Agent-to-Agent 智能体通信协议 |
| Token 用量可观测 | demo14 Advisor 自动计量：调用数/输入输出 Token/延迟 P50·P95/成本估算 |
| VitePress 文档站 | 教学文档 + 自定义主题 + 学习路径时间线 |

### 教学简化（生产需增强）

| 简化点 | 生产做法 |
|--------|----------|
| 向量存储用 SimpleVectorStore（内存） | Milvus / Elasticsearch / PgVector |
| 会话记忆用内存 Map | Redis 分布式会话 |
| 工具数据硬编码在 Map | 数据库 / 微服务查询 |
| 无认证鉴权 | Spring Security + JWT |

### 未实现（需真实 API Key）

| 功能 | 说明 |
|------|------|
| AI 模型真实调用 | 需配置 DashScope API Key |
| 向量数据库持久化 | 需部署 Milvus / ES 等 |

## 测试覆盖

| 测试类 | 用例数 | 覆盖场景 |
|--------|--------|----------|
| `WeatherToolsTest` | 2 | 已知城市天气、未知城市兜底 |
| `AfterSalesToolTest` | 5 | 4 种商品政策查询、未知商品兜底 |
| `ShopServiceToolTest` | 8 | 套餐查询、全部套餐列表、优惠活动、空参默认 |
| `GlobalExceptionHandlerTest` | 6 | ApiResponse 包装、校验异常、业务异常、未知异常 |
| `TokenUsageMetricsTest` | 9 | 计量累计、null 容错、延迟分位、成本估算、单价未配置提示、清零、环形缓冲 |
| `TokenUsageAdvisorTest` | 4 | 用量入账、响应透传、失败记账+异常上抛、元数据缺失容错 |
| **合计** | **34** | |

```bash
mvn test
```

> demo14 使用示例：
> ```bash
> curl "http://localhost:8080/api/demo14/chat?message=用一句话介绍通义千问"
> curl http://localhost:8080/api/demo14/usage
> ```
> 在 application.yml 配置 `demo14.prompt-price-per-million` / `completion-price-per-million` 后即可看到估算成本。

## License

仅用于教学交流，作者：ibqy

<p align="center">
  <a href="https://github.com/ibqy">🏠 回到 ibqy 主页</a> · <a href="https://ibqy.github.io">🌐 作品集</a>
</p>
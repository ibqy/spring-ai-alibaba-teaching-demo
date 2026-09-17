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

## 实现边界

### 已实现（可直接运行）

| 功能 | 说明 |
|------|------|
| 11 个渐进式 Demo | 基础对话 → 流式 → 结构化输出 → 工具调用 → RAG → 综合客服 |
| Function Calling | 天气查询 / 售后政策 / 订餐服务 3 类工具 |
| RAG 检索增强 | SimpleVectorStore + 知识库文档 |
| 会话记忆 | MessageWindowChatMemory（20 条窗口） |
| DashScope 联网搜索 | `DashScopeChatOptions.withEnableSearch(true)` |
| 流式 SSE 输出 | `Flux<String>` + `text/event-stream` |
| 全局异常处理 | `@ControllerAdvice` + `ApiResponse` 统一响应包装 |
| 参数校验 | Jakarta Validation（`@NotBlank`）+ `@Valid` |
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
| 多 Agent 协作 | Spring AI Alibaba Agent Framework |
| 向量数据库持久化 | 需部署 Milvus / ES 等 |

## 测试覆盖

| 测试类 | 用例数 | 覆盖场景 |
|--------|--------|----------|
| `WeatherToolsTest` | 2 | 已知城市天气、未知城市兜底 |
| `AfterSalesToolTest` | 5 | 4 种商品政策查询、未知商品兜底 |
| `ShopServiceToolTest` | 8 | 套餐查询、全部套餐列表、优惠活动、空参默认 |
| `GlobalExceptionHandlerTest` | 6 | ApiResponse 包装、校验异常、业务异常、未知异常 |
| **合计** | **21** | |

```bash
mvn test
```

## License

仅用于教学交流，作者：ibqy

<p align="center">
  <a href="https://github.com/ibqy">🏠 回到 ibqy 主页</a> · <a href="https://ibqy.github.io">🌐 作品集</a>
</p>
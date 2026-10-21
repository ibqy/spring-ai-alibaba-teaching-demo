# 05 · MCP 与 A2A

> 前置知识：demo05（函数调用） | 涉及 Demo：demo12（MCP）、demo13（A2A）
> 学习目标：理解 MCP 和 A2A 两大协议，让 Agent 既能调用工具，又能与其他 Agent 协作

## 1. MCP：AI 的 USB-C 接口

> **MCP（Model Context Protocol）** 是 Anthropic 2024 年提出的 AI 应用与外部工具之间的标准协议。

```
┌──────────┐    MCP     ┌──────────────┐
│ Spring AI│ ←────────→ │ MCP Server   │
│ Alibaba  │  stdio/SSE │ (文件系统…)   │
└──────────┘            └──────────────┘
```

### 1.1 依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 1.2 配置

```yaml
spring:
  ai:
    mcp:
      client:
        enabled: ${MCP_ENABLED:false}
        stdio:
          servers:
            filesystem:
              command: npx
              args: ["-y", "@modelcontextprotocol/server-filesystem", "."]
```

### 1.3 运行

**代码**：[Demo12McpClientController](../src/main/java/com/xb/springai/controller/demo12/Demo12McpClientController.java)（含 `/status` 自查端点）

```bash
export MCP_ENABLED=true
mvn spring-boot:run
curl "http://localhost:8080/api/demo12/ask?question=查看项目目录结构"
```

## 2. A2A：Agent 的外交通道

> **A2A（Agent-to-Agent Protocol）** 是 Google 2025 年提出的智能体间通信标准。

| 维度 | MCP | A2A |
|------|-----|-----|
| 解决什么 | AI ↔ 工具 | Agent ↔ Agent |
| 核心概念 | Tool / Server | AgentCard / JSON-RPC |
| 提出者 | Anthropic | Google |

### 2.1 依赖

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>spring-ai-a2a-server-autoconfigure</artifactId>
    <version>0.3.0</version>
</dependency>
```

### 2.2 配置

```yaml
spring:
  ai:
    a2a:
      server:
        enabled: ${A2A_ENABLED:false}
```

### 2.3 运行

**代码**：[Demo13A2aController](../src/main/java/com/xb/springai/controller/demo13/Demo13A2aController.java)

```bash
export A2A_ENABLED=true
mvn spring-boot:run
# 查看 AgentCard
curl http://localhost:8080/a2a/card
# 本地测试
curl "http://localhost:8080/api/demo13/ask?message=你好"
```

## 3. 小结

- **MCP** 让 Agent 调用外部工具（USB-C 接口）
- **A2A** 让 Agent 之间互相发现和调用（外交通道）
- 二者互补：**MCP + A2A = Agent 的完整连接能力**
- Spring AI Alibaba 可以同时支持两大协议

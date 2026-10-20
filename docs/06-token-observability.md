# 06 · Token 用量可观测（demo14）

> 前置：demo11（能力组合）｜涉及 Demo：demo14
> 目标：理解 Advisor 横切机制，给 AI 应用装上"电表"——Token 用量、延迟分位、成本估算。

## 1. 为什么这是生产第一课

大模型按 Token 计费。没有用量监控，就等于：

- 不知道钱花在哪（哪个功能、哪个用户）
- 无法发现异常消耗（死循环调工具、被恶意刷量）
- 容量与成本规划全靠猜

demo01~demo13 教的是"怎么把能力跑起来"；demo14 教的是"能力上线后怎么看得住"。

## 2. Advisor：横切关注点的收口机制

Spring AI 的每个响应都带 `Usage` 元数据（输入/输出 Token 数），问题只是"在哪收"。

**方案对比：**

| 方案 | 问题 |
|---|---|
| 写在 Controller 里 | demo01~13 每个端点抄一遍，新增端点必漏 |
| AOP 切 `ChatModel` | 切点难选，且拿不到 ChatClient 层上下文 |
| **Advisor** | 挂在 ChatClient 上，一次装配全局生效 |

Advisor 像 AOP 切面，能在"调用大模型"前后插入逻辑：

```
请求 ──► [Advisor 链] ──► ChatModel ──► 响应
              │
     TokenUsageAdvisor
     ┌────────────────────────┐
     │ before: 记录开始时间      │
     │ after:  从 Usage 累计    │
     └────────────────────────┘
```

## 3. 实现要点（三段式）

### 3.1 计量核心（TokenUsageMetrics）—— 纯逻辑，可离线单测

- `LongAdder` 计数：Advisor 是单例，并发请求同时写，多写少读场景比 `AtomicLong` 争用小
- 延迟分位：固定容量环形缓冲（512）+ 读时排序，P50/P95 比平均值更能反映"慢请求有多慢"
- 成本估算：按"元/百万 token"单价，未配置时按 0 展示并说明原因，不显示误导性数字

### 3.2 计量 Advisor（TokenUsageAdvisor）—— 装饰器，只读不改

```java
public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    long start = System.nanoTime();
    try {
        ChatClientResponse response = chain.nextCall(request);   // 先放行
        var usage = response.chatResponse().getMetadata().getUsage();
        metrics.record(System.nanoTime() - start, usage);        // 再记账
        return response;                                          // 原样返回
    } catch (RuntimeException e) {
        metrics.record(System.nanoTime() - start, null);         // 失败也记延迟
        throw e;                                                  // 异常上抛
    }
}
```

三条铁律：**先放行再记账**（不能吞响应）、**只读不改**（装饰器透明）、**失败也记账**（超时耗时是排查线索）。

### 3.3 端点（Demo14TokenObservabilityController）

```bash
# 产生调用（自动计量）
curl "http://localhost:8080/api/demo14/chat?message=用一句话介绍通义千问"

# 查看用量
curl http://localhost:8080/api/demo14/usage
```

```json
{
  "callCount": 1,
  "promptTokens": 9,
  "completionTokens": 16,
  "totalTokens": 25,
  "avgTokensPerCall": 25,
  "llmLatencyP50Ms": 268,
  "llmLatencyP95Ms": 268,
  "estimatedCostYuan": 0.0,
  "priceNote": "未配置模型单价（application.yml 的 demo14.*.price-per-million），成本按 0 展示"
}
```

## 4. 容错分支（都是实测踩出来的）

| 情况 | 处理 |
|---|---|
| `getUsage()` 返回 null（部分代理不返回） | 判空按 0 计，不 NPE |
| `Usage` 字段为 null（Spring AI 1.x 是 `Integer`） | 逐字段判空 |
| 调用抛异常（超时/401） | 补记无用量记录，异常原样抛 |
| 单价未配置 | 成本 0 + priceNote 说明，不抛异常 |

## 5. 从 Demo 到生产

1. **外部化存储**：计数器换 Redis / Prometheus，重启不丢
2. **维度拆分**：按 conversationId / userId 分桶——Advisor 里能从请求上下文拿到
3. **实时告警**：单用户 Token 速率异常触发限流
4. **预算熔断**：租户级 Token 预算，超限拒绝（参考 agent-orchestration-demo 的 TokenBudget）
5. **计费对账**：聚合数据与厂商账单按日核对

## 6. 测试策略

demo14 的 13 个单测分两层：

- `TokenUsageMetricsTest`（9）：喂真实 `Usage` 对象验证累计/分位/成本/容错——不依赖 Spring 也不依赖模型
- `TokenUsageAdvisorTest`（4）：mock 调用链，验证"记账不打扰主流程"——响应透传、失败上抛、元数据缺失容错

> Advisor 夹在业务与模型之间，写错（吞响应、咽异常）会影响所有端点，但编译看不出来——必须单测。

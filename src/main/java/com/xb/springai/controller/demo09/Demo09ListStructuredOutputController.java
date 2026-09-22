package com.xb.springai.controller.demo09;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Demo09 - Agent 列表结构化输出（批量提取）
 *
 * 演示 ReactAgent 如何处理列表输出：
 * 1. outputType 方式：用包装类 OrderList 持有 List<Demo09Order>
 * 2. outputSchema 方式：手动指定 JSON Schema，直接返回数组
 */
@RestController
@RequestMapping("/api/demo09")
public class Demo09ListStructuredOutputController {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Demo09ListStructuredOutputController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * 方式一：outputType + 包装类（推荐）
     * 框架根据 OrderList 自动生成 Schema，返回 {"items": [...]}
     */
    @GetMapping("/orders")
    public List<Demo09Order> parse(@RequestParam(defaultValue = "订单A1001买了咖啡30元已支付，"
            + "订单B2002买了茶叶88元待支付，订单C3003买了杯子25元已发货") String text) throws Exception {

        ReactAgent agent = ReactAgent.builder()
                .name("order_extractor")
                .model(chatModel)
                .outputType(OrderList.class)
                .saver(new MemorySaver())
                .build();

        AssistantMessage message = agent.call(String.format(
                "请从中抽取所有订单，字段包括 orderId/item/price/status：%s", text));

        String json = stripCodeFence(message.getText());
        OrderList orderList = objectMapper.readValue(json, OrderList.class);
        return orderList.getItems();
    }

    /**
     * 方式二：outputSchema 手动指定 JSON Schema
     * 直接约束模型返回 JSON 数组，无需包装类
     */
    @GetMapping("/orders-schema")
    public List<Demo09Order> parseWithSchema(@RequestParam(defaultValue = "订单A1001买了咖啡30元已支付，"
            + "订单B2002买了茶叶88元待支付") String text) throws Exception {

        String schema = """
                {
                  "type": "array",
                  "items": {
                    "type": "object",
                    "properties": {
                      "orderId": { "type": "string" },
                      "item": { "type": "string" },
                      "price": { "type": "number" },
                      "status": { "type": "string" }
                    },
                    "required": ["orderId", "item", "price", "status"]
                  }
                }
                """;

        ReactAgent agent = ReactAgent.builder()
                .name("order_schema_extractor")
                .model(chatModel)
                .outputSchema(schema)
                .saver(new MemorySaver())
                .build();

        AssistantMessage message = agent.call(String.format(
                "请从中抽取所有订单：%s", text));

        String json = stripCodeFence(message.getText());
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Demo09Order.class));
    }

    private String stripCodeFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }
        return trimmed;
    }
}

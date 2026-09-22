package com.xb.springai.controller.demo03;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo03 - Agent 结构化输出（单对象）
 *
 * 演示 ReactAgent.outputType() 的用法：框架根据 POJO 自动生成 JSON Schema，
 * 注入提示词引导模型按指定格式返回，再用 Jackson 反序列化为 Java 对象。
 */
@RestController
@RequestMapping("/api/demo03")
public class Demo03StructuredOutputController {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Demo03StructuredOutputController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/actor")
    public ActorFilm actor(@RequestParam(defaultValue = "周星驰") String actor) throws Exception {
        ReactAgent agent = ReactAgent.builder()
                .name("actor_extractor")
                .model(chatModel)
                .outputType(ActorFilm.class)
                .saver(new MemorySaver())
                .build();

        AssistantMessage message = agent.call(String.format(
                "请介绍演员 %s，字段：name(姓名)、genre(代表作类型)、famousWork(代表作)、reason(推荐理由)。",
                actor));

        String json = stripCodeFence(message.getText());
        return objectMapper.readValue(json, ActorFilm.class);
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

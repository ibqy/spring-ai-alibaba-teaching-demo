package com.xb.springai.controller.demo09;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * demo09：批量结构化输出 —— 一次从文本里抽出"多张"订单
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>升级点</b>：demo03 只抽<b>一个</b>对象；这里用
 * <code>entity(new ParameterizedTypeReference&lt;List&lt;Demo09Order&gt;&gt;() {})</code>，
 * 要求模型把一整段文本里<b>所有</b>订单都解析为 JSON 数组。</p>
 *
 * <p><b>典型场景</b>：把用户粘贴的聊天记录 / 邮件 / 单据文本，自动转换为结构化数据入库。</p>
 *
 * <p><b>运行</b>：<code>GET /api/demo09/orders?text=订单A1001买了咖啡30元已支付，订单B2002买了茶叶88元待支付</code></p>
 *
 * @author ibqy
 */
@RestController
@RequestMapping("/api/demo09")
public class Demo09ListStructuredOutputController {

    private final ChatClient chatClient;

    public Demo09ListStructuredOutputController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 从自然语言文本中批量抽取订单信息。
     *
     * <p>通过 {@code ParameterizedTypeReference<List<Demo09Order>>} 告诉 Spring AI
     * 期望的返回类型是列表，框架会自动引导模型输出 JSON 数组并反序列化。</p>
     *
     * @param text 包含订单信息的自然语言文本
     * @return 解析出的订单列表
     */
    @GetMapping("/orders")
    public List<Demo09Order> parse(@RequestParam(defaultValue = "订单A1001买了咖啡30元已支付，"
            + "订单B2002买了茶叶88元待支付，订单C3003买了杯子25元已发货") String text) {

        // 关键：ParameterizedTypeReference 表达"泛型 List<Demo09Order>"
        return chatClient.prompt()
                .user(String.format(
                        "请从中抽取所有订单，字段包括 orderId/item/price/status，"
                                + "只输出 JSON 数组，不要返回其它内容：%s", text))
                .call()
                .entity(new ParameterizedTypeReference<List<Demo09Order>>() {})
                ;
    }
}
package com.xb.springai.controller.demo10;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

/**
 * 售后政策工具 —— 客服机器人用来查询退换货规则的"数据库"
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p>模拟一个售后政策表：商品 -> 可退/可换天数。真实项目里这里通常是查数据库。</p>
 */
public class AfterSalesTool {

    private final Map<String, String> policy = Map.of(
            "咖啡豆", "7 天内无理由退货，已拆封不支持",
            "咖啡杯", "30 天内可退换，需保留包装",
            "手冲壶", "15 天内非人为损坏可换新",
            "挂耳咖啡", "非质量问题不支持退货"
    );

    /**
     * 查询某商品的售后政策。
     */
    @Tool(name = "query_after_sales", description = "查询某商品的退换货政策，返回可退换天数与限制")
    public String queryAfterSales(@ToolParam(required = true, description = "商品名称，例如：咖啡豆") String item) {
        String rule = policy.getOrDefault(item, "暂未收录「" + item + "」的售后政策，请以店铺公告为准。");
        return "商品【" + item + "】：" + rule;
    }
}
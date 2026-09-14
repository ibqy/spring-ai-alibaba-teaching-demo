package com.xb.springai.controller.demo09;

/**
 * 批量结构化输出用的 DTO —— 一条订单记录
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p>结合 demo10 的 {@code ParameterizedTypeReference<List<Demo09Order>>}，
 * 让模型一次输出多张订单的 JSON 数组。</p>
 */
public record Demo09Order(
        String orderId,  // 订单号
        String item,     // 商品名
        Double price,    // 价格
        String status    // 状态：待支付/已支付/已发货等
) {
}
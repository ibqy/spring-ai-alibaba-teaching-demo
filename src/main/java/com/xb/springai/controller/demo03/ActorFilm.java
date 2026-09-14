package com.xb.springai.controller.demo03;

/**
 * 结构化输出用的 Java Record —— 让大模型输出严格"长"成这个形状
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>为什么要 record</b>：Java 16+ 的 record 天然适合做 DTO，字段即构造参数。
 * Spring AI 的 <code>entity(Class)</code> 会把模型的 JSON 输出自动反序列化成这个对象，
 * 我们无需写任何手动解析代码。</p>
 */
public record ActorFilm(
        String name,       // 演员/导演姓名
        String genre,      // 代表作类型
        String famousWork, // 代表作
        String reason      // 推荐理由
) {
}
package com.xb.springai.controller.demo05;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.Map;

/**
 * 函数调用示例 —— 一个"查天气"的工具 Bean
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>它解决什么问题</b>：大模型不知道实时天气。我们把{@link #getWeather(String, String)}
 * 这个方法暴露成"工具"，当用户问天气时，模型会<b>主动调用这个方法</b>拿到真实数据再回答。</p>
 *
 * <p>真实项目里，@Tool 方法通常去查数据库/第三方 API，这里用 Map 模拟。</p>
 */
public class WeatherTools {

    /**
     * 模拟的天气数据：城市 -> (天气, 温度)
     */
    private final Map<String, String> weatherData = Map.of(
            "北京", "晴，25 度",
            "上海", "多云，28 度",
            "杭州", "小雨，22 度",
            "广州", "雷阵雨，30 度"
    );

    /**
     * 查询指定城市的天气。被 @Tool 标注后，模型可自动调用。
     *
     * @param city 城市名称，如"北京"（必填）
     * @param date 日期，如"今天"（可选）
     * @return 天气描述文本
     */
    @Tool(name = "get_weather", description = "根据城市名查询今天的实时天气，city 为城市名称")
    public String getWeather(
            @ToolParam(required = true, description = "城市名称，例如：北京") String city,
            @ToolParam(description = "日期，例如：今天、明天，可省略") String date) {

        String base = weatherData.getOrDefault(city, "暂无「" + city + "」的天气数据");
        // date 为空时只返回基础天气；有日期时拼接上日期
        return (date == null || date.isBlank() ? "" : date + "，") + city + "：" + base;
    }
}
package com.xb.springai.controller.demo05;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * WeatherTools 的离线单元测试 —— 不依赖真实大模型，纯逻辑验证
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 */
class WeatherToolsTest {

    @Test
    void queryKnownCityReturnsWeather() {
        WeatherTools tools = new WeatherTools();
        String result = tools.getWeather("北京", "今天");
        assertNotNull(result);
        assertTrue(result.contains("北京"));
        assertTrue(result.contains("今天"));
        assertTrue(result.contains("晴"));
    }

    @Test
    void queryUnknownCityReturnsFallback() {
        WeatherTools tools = new WeatherTools();
        String result = tools.getWeather("不存在的城市", null);
        assertTrue(result.contains("暂无"));
        assertFalse(result.contains("从外星球")); // 不应编造
    }
}
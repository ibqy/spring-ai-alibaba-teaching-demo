package com.xb.springai.controller.demo10;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AfterSalesTool 售后政策工具测试")
class AfterSalesToolTest {

    private final AfterSalesTool tool = new AfterSalesTool();

    @Test
    @DisplayName("已知商品返回对应售后政策")
    void queryAfterSales_knownItem_returnsPolicy() {
        String result = tool.queryAfterSales("咖啡豆");
        assertTrue(result.contains("咖啡豆"));
        assertTrue(result.contains("7 天"));
    }

    @Test
    @DisplayName("咖啡杯售后政策包含30天")
    void queryAfterSales_coffeeCup_30days() {
        String result = tool.queryAfterSales("咖啡杯");
        assertTrue(result.contains("30 天"));
    }

    @Test
    @DisplayName("手冲壶售后政策包含15天换新")
    void queryAfterSales_pourOver_kettle() {
        String result = tool.queryAfterSales("手冲壶");
        assertTrue(result.contains("15 天"));
        assertTrue(result.contains("换新"));
    }

    @Test
    @DisplayName("未知商品返回未收录提示")
    void queryAfterSales_unknownItem_fallback() {
        String result = tool.queryAfterSales("不存在的商品");
        assertTrue(result.contains("暂未收录"));
        assertTrue(result.contains("不存在的商品"));
    }

    @Test
    @DisplayName("所有已知商品均可查询")
    void queryAfterSales_allKnownItems() {
        for (String item : new String[]{"咖啡豆", "咖啡杯", "手冲壶", "挂耳咖啡"}) {
            String result = tool.queryAfterSales(item);
            assertTrue(result.contains(item), "结果应包含商品名: " + item);
            assertFalse(result.contains("暂未收录"), item + " 应为已知商品");
        }
    }
}

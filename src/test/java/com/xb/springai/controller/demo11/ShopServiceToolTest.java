package com.xb.springai.controller.demo11;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ShopServiceTool 订餐服务工具测试")
class ShopServiceToolTest {

    private final ShopServiceTool tool = new ShopServiceTool();

    // ========== queryMenu ==========

    @Test
    @DisplayName("查询已知套餐返回价格和库存")
    void queryMenu_knownSet_returnsPriceAndStock() {
        String result = tool.queryMenu("午市单人餐");
        assertTrue(result.contains("午市单人餐"));
        assertTrue(result.contains("39"));
        assertTrue(result.contains("在售"));
    }

    @Test
    @DisplayName("查询所有已知套餐均成功")
    void queryMenu_allKnownSets() {
        for (String name : new String[]{"午市单人餐", "双人下午茶", "手冲尝鲜套餐", "周末家庭套餐", "打包带走特惠"}) {
            String result = tool.queryMenu(name);
            assertTrue(result.contains(name));
            assertFalse(result.contains("暂未查到"));
        }
    }

    @Test
    @DisplayName("查询未知套餐返回未查到提示")
    void queryMenu_unknownSet_fallback() {
        String result = tool.queryMenu("不存在的套餐");
        assertTrue(result.contains("暂未查到"));
        assertTrue(result.contains("不存在的套餐"));
    }

    // ========== listMenu ==========

    @Test
    @DisplayName("列出全部套餐")
    void listMenu_containsAllSets() {
        String result = tool.listMenu();
        assertTrue(result.contains("午市单人餐"));
        assertTrue(result.contains("双人下午茶"));
        assertTrue(result.contains("手冲尝鲜套餐"));
        assertTrue(result.contains("周末家庭套餐"));
        assertTrue(result.contains("打包带走特惠"));
    }

    @Test
    @DisplayName("列表包含在售状态")
    void listMenu_showsStockStatus() {
        String result = tool.listMenu();
        assertTrue(result.contains("在售"));
    }

    // ========== queryActivity ==========

    @Test
    @DisplayName("查询今天优惠返回日期和描述")
    void queryActivity_today_returnsDescription() {
        String result = tool.queryActivity("今天");
        assertNotNull(result);
        assertTrue(result.contains("的优惠"));
    }

    @Test
    @DisplayName("查询明天优惠返回日期和描述")
    void queryActivity_tomorrow_returnsDescription() {
        String result = tool.queryActivity("明天");
        assertNotNull(result);
        assertTrue(result.contains("的优惠"));
    }

    @Test
    @DisplayName("null参数默认查今天")
    void queryActivity_null_defaultsToToday() {
        String result = tool.queryActivity(null);
        assertNotNull(result);
        assertTrue(result.contains("的优惠"));
    }
}

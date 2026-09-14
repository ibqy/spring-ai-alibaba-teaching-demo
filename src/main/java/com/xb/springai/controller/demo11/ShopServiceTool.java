package com.xb.springai.controller.demo11;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * demo11 · 工具类：ShopServiceTool —— 模拟"订餐/套餐"业务数据查询工具
 *
 * <p>作者：ibqy | 日期：2026-09-10</p>
 *
 * <p><b>它在这个"知识型订餐客服"里扮演什么角色？</b></p>
 * <p>客服机器人有两类信息来源：</p>
 * <ul>
 *   <li>《静态资料》(RAG)：营业时间、地址、招牌饮品的<b>固定文案</b> —— 已固化在 kb/shop-intro.txt。</li>
 *   <li>《实时业务数据》(工具)：套餐价格、库存、优惠，这些是<b>会变的</b>、且直接决定顾客要不要下单 —— 必须靠工具即时查。</li>
 * </ul>
 * <p>这就是<b>为什么要把 RAG 和 工具 同时挂上</b>：资料答"你卖什么、几点开"，
 * 工具答"现在多少钱、有没有货、能优惠多少"，两者互补，才是一个能落地的订餐客服。</p>
 *
 * <p><b>写法参考</b>：{@code demo05.WeatherTools} / {@code demo10.AfterSalesTool}，
 * 用 {@code @Tool} 标注方法，模型即可在需要时自动调用（Function Calling）。</p>
 */
public class ShopServiceTool {

    /**
     * 模拟"套餐"数据：套餐名 -> (价格, 库存)。
     * 真实项目这里应该是查数据库/调用后端微服务；内存 Map 仅用于教学演示。
     */
    private static final Map<String, MenuSet> MENU_SET = Map.of(
            "午市单人餐", new MenuSet(39.0, 12),
            "双人下午茶", new MenuSet(88.0, 5),
            "手冲尝鲜套餐", new MenuSet(58.0, 20),
            "周末家庭套餐", new MenuSet(168.0, 3),
            "打包带走特惠", new MenuSet(29.0, 50)
    );

    /**
     * 优惠活动：星期几 -> 优惠描述。周期类优惠用工具查，比写死在资料里更贴近真实业务。
     */
    private static final Map<Integer, String> WEEK_ACTIVITY = Map.of(
            1, "周一会员日：全场套餐 9 折",
            5, "周五咖啡日：手冲类套餐第二份半价",
            7, "周日家庭日：家庭套餐立减 20 元"
    );

    /**
     * 查询某套餐的价格、库存与是否在售。
     *
     * @param setName 套餐名称，例如：双人下午茶
     * @return 包含价格与库存的文字描述
     */
    @Tool(name = "query_menu", description = "查询某个套餐的价格、库存与是否在售。顾客想点餐、问价格、问有没有货时调用")
    public String queryMenu(@ToolParam(required = true, description = "套餐名称，例如：双人下午茶") String setName) {
        MenuSet ms = MENU_SET.get(setName);
        if (ms == null) {
            return "暂未查到套餐【" + setName + "】，请回复顾客：店里没有这个套餐，可推荐套餐列表。";
        }
        String status = ms.stock > 0 ? "在售（剩余 " + ms.stock + " 份）" : "暂时售罄";
        return String.format("套餐【%s】：价格 %.0f 元，状态：%s。", setName, ms.price, status);
    }

    /**
     * 查询"今天"或"明天"的优惠活动。
     *
     * @param when 时间，如"今天""明天"；为空则查今天
     * @return 当日优惠文案
     */
    @Tool(name = "query_activity", description = "根据日期查询当天优惠活动。顾客问有什么优惠、最近活动、多少钱更划算时调用")
    public String queryActivity(@ToolParam(description = "时间：今天 或 明天，可省略，省略则查今天") String when) {
        LocalDate date = (when != null && when.contains("明天")) ? LocalDate.now().plusDays(1) : LocalDate.now();
        // 取星期几：DayOfWeek 的 getValue() 返回 1~7（周一=1 ... 周日=7）
        int dow = date.getDayOfWeek().getValue();
        String activity = WEEK_ACTIVITY.getOrDefault(dow, "今天是工作日平销，暂无专属优惠，会员积分照常累计。");
        return date + " 的优惠：" + activity;
    }

    /**
     * 查看当前在售的全部套餐列表（供顾客挑选）。
     *
     * @return 每个套餐名一行
     */
    @Tool(name = "list_menu", description = "列出店里当前在售的全部套餐名称。顾客问你们有什么、能点些什么时调用")
    public String listMenu() {
        List<String> lines = MENU_SET.keySet().stream()
                .map(name -> "- " + name + "：" + (MENU_SET.get(name).stock > 0 ? "在售" : "售罄"))
                .toList();
        return "当前在售套餐：\n" + String.join("\n", lines);
    }

    /**
     * 内部小结构体：一个套餐的价格与库存。
     */
    private record MenuSet(double price, int stock) {
    }
}
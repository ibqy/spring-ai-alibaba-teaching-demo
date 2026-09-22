package com.xb.springai.controller.demo09;

import java.util.List;

/**
 * Demo09 列表包装类 - ReactAgent.outputType() 只接受 Class<?>，
 * 无法直接表达 List<Demo09Order> 这样的泛型类型（Java 泛型擦除），
 * 因此用一个包装类持有列表字段，框架会自动生成包含 items 数组的 JSON Schema。
 */
public class OrderList {

    private List<Demo09Order> items;

    public OrderList() {
    }

    public List<Demo09Order> getItems() {
        return items;
    }

    public void setItems(List<Demo09Order> items) {
        this.items = items;
    }
}

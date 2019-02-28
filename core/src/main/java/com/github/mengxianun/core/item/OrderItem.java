package com.github.mengxianun.core.item;

import com.github.mengxianun.core.json.Order;

public class OrderItem extends Item {

	private static final long serialVersionUID = 1L;
	private ColumnItem columnItem;
	// 排序方式
	private Order order;

	public OrderItem(ColumnItem columnItem, Order order) {
		this.columnItem = columnItem;
		this.order = order;
	}

	public ColumnItem getColumnItem() {
		return columnItem;
	}

	public void setColumnItem(ColumnItem columnItem) {
		this.columnItem = columnItem;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}

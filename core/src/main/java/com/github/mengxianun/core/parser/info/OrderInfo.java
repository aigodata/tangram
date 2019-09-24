package com.github.mengxianun.core.parser.info;

import com.github.mengxianun.core.request.Order;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OrderInfo {

	public static OrderInfo create(Order order, ColumnInfo columnInfo) {
		return new AutoValue_OrderInfo(order, columnInfo);
	}

	public abstract Order order();

	public abstract ColumnInfo columnInfo();

}

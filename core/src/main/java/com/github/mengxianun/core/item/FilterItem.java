package com.github.mengxianun.core.item;

import java.util.Collections;
import java.util.List;

import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operator;

public class FilterItem extends ValuesItem {

	private static final long serialVersionUID = 1L;
	// 条件列
	private final ColumnItem columnItem;
	// 连接符, AND/OR
	private final Connector connector;
	// 运算符
	private final Operator operator;
	// 子条件
	private final List<FilterItem> subFilterItems;

	public FilterItem(Connector connector, ColumnItem columnItem, Operator operator, Object value) {
		this(connector, columnItem, operator, value, Collections.emptyList());
	}

	public FilterItem(Connector connector, List<FilterItem> subFilterItems) {
		this(connector, null, null, null, subFilterItems);
	}

	private FilterItem(Connector connector, ColumnItem columnItem, Operator operator, Object value,
			List<FilterItem> subFilterItems) {
		super(value);
		this.connector = connector;
		this.columnItem = columnItem;
		this.operator = operator;
		this.subFilterItems = subFilterItems;
	}

	public void addSubFilterItem(FilterItem subFilterItem) {
		this.subFilterItems.add(subFilterItem);
	}

	public ColumnItem getColumnItem() {
		return columnItem;
	}

	public Connector getConnector() {
		return connector;
	}

	public Operator getOperator() {
		return operator;
	}

	public List<FilterItem> getSubFilterItems() {
		return subFilterItems;
	}

	public Object getRealValue() {
		// Like must be string
		if (operator == Operator.LIKE) {
			return value;
		}
		return getRealValue(columnItem.getColumn(), value);
	}

}

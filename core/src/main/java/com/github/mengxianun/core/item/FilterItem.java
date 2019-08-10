package com.github.mengxianun.core.item;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operator;

public class FilterItem extends ValuesItem {

	private static final long serialVersionUID = 1L;
	// 条件列
	private ColumnItem columnItem;
	// 连接符, AND/OR
	private Connector connector;
	// 运算符
	private Operator operator;
	// 子条件
	private List<FilterItem> subFilterItems;

	public FilterItem() {
		super(null);
		this.connector = Connector.AND;
		this.subFilterItems = new ArrayList<>();
	}

	public FilterItem(ColumnItem columnItem, Object value, Connector connector, Operator operator) {
		this();
		this.columnItem = columnItem;
		this.value = value;
		this.connector = connector;
		this.operator = operator;
	}

	public void addSubFilterItem(FilterItem subFilterItem) {
		this.subFilterItems.add(subFilterItem);
	}

	public ColumnItem getColumnItem() {
		return columnItem;
	}

	public void setColumnItem(ColumnItem columnItem) {
		this.columnItem = columnItem;
	}

	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public List<FilterItem> getSubFilterItems() {
		return subFilterItems;
	}

	public void setSubFilterItems(List<FilterItem> subFilterItems) {
		this.subFilterItems = subFilterItems;
	}

	public Object getRealValue() {
		return getRealValue(columnItem.getColumn(), value);
	}

}

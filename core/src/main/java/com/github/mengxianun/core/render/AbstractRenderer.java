package com.github.mengxianun.core.render;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;

public abstract class AbstractRenderer<T> implements Renderer<T> {

	protected final Action action;

	public AbstractRenderer(Action action) {
		this.action = action;
	}

	protected String getColumnKey(ColumnItem columnItem) {
		String columnKey = "";
		Column column = columnItem.getColumn();
		if (columnItem.isCustomAlias()) { // 自定义别名
			columnKey = columnItem.getAlias();
		} else if (column == null) { // 表达式, 如函数
			columnKey = columnItem.getExpression();
		} else {
			columnKey = App.Context.getColumnAlias(column);
		}
		return columnKey;
	}

}

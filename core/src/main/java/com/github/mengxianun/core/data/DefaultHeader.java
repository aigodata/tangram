package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;
import com.google.common.base.Strings;

public class DefaultHeader implements Header {

	private final List<ColumnItem> items;

	public DefaultHeader(List<ColumnItem> items) {
		this.items = items;
	}

	@Override
	public List<ColumnItem> getColumnItems() {
		return items;
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	public ColumnItem getColumnItem(int index) {
		return items.get(index);
	}

	@Override
	public int indexOf(String columnNameOrAlias) {
		for (int i = 0; i < items.size(); i++) {
			ColumnItem columnItem = items.get(i);
			Column column = columnItem.getColumn();
			String expression = columnItem.getExpression();
			String alias = columnItem.getAlias();
			boolean isAlias = !Strings.isNullOrEmpty(alias) && alias.equals(columnNameOrAlias);
			boolean isColumn = column != null && column.getName().equals(columnNameOrAlias);
			boolean isExpression = !Strings.isNullOrEmpty(expression) && expression.equals(columnNameOrAlias);
			if (isAlias || isColumn || isExpression) {
				return i;
			}
		}
		return -1;
	}

}

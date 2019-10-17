package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;
import com.google.common.base.Strings;

public abstract class AbstractRow implements Row {

	private final Header header;

	public AbstractRow(Header header) {
		this.header = header;
	}

	@Override
	public Header getHeader() {
		return header;
	}

	@Override
	public Object getValue(int index) {
		return getValues()[index];
	}

	@Override
	public int size() {
		return header.size();
	}

	@Override
	public List<ColumnItem> getColumnItems() {
		return header.getColumnItems();
	}

	protected int indexOf(ColumnItem item) {
		List<ColumnItem> items = header.getColumnItems();
		for (int i = 0; i < items.size(); i++) {
			if (item == items.get(i)) {
				return i;
			}
		}
		return -1;
	}

	protected int indexOf(Column column) {
		List<ColumnItem> items = header.getColumnItems();
		for (int i = 0; i < items.size(); i++) {
			if (column == items.get(i).getColumn()) {
				return i;
			}
		}
		return -1;
	}

	protected int indexOf(String columnName) {
		if (Strings.isNullOrEmpty(columnName)) {
			return -1;
		}
		List<ColumnItem> items = header.getColumnItems();
		for (int i = 0; i < items.size(); i++) {
			if (columnName.equals(items.get(i).getAlias())) {
				return i;
			}
		}
		for (int i = 0; i < items.size(); i++) {
			Column column = items.get(i).getColumn();
			if (column != null && columnName.equals(column.getName())) {
				return i;
			}
		}
		for (int i = 0; i < items.size(); i++) {
			if (columnName.equals(items.get(i).getExpression())) {
				return i;
			}
		}
		return -1;
	}

}

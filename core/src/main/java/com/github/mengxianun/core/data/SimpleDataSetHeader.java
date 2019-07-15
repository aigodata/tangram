package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;

public class SimpleDataSetHeader implements DataSetHeader {

	private final List<ColumnItem> items;

	public SimpleDataSetHeader(List<ColumnItem> items) {
		this.items = items;
	}

	@Override
	public List<ColumnItem> getColumnItems() {
		return items;
	}

	@Override
	public ColumnItem getColumnItem(int index) {
		return items.get(index);
	}

	@Override
	public int size() {
		return items.size();
	}

}

package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;

public abstract class AbstractRow implements Row {

	private final DataSetHeader header;

	public AbstractRow(DataSetHeader header) {
		this.header = header;
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

}

package com.github.mengxianun.core.data;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;

public class DefaultRow extends AbstractRow {

	private final Object[] values;

	public DefaultRow(Header header, Object[] values) {
		super(header);
		this.values = values;
	}

	@Override
	public Object getValue(ColumnItem item) {
		return values[indexOf(item)];
	}

	@Override
	public Object getValue(Column column) {
		return values[indexOf(column)];
	}

	@Override
	public Object getValue(String columnName) {
		return values[indexOf(columnName)];
	}

	@Override
	public Object[] getValues() {
		return values;
	}

}

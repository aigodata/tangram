package com.github.mengxianun.core.item;

import com.github.mengxianun.core.schema.Column;

public class ValueItem extends ValuesItem {

	private static final long serialVersionUID = 1L;
	private Column column;

	public ValueItem(Column column, Object value) {
		super(value);
		this.column = column;
	}

	public Column getColumn() {
		return column;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	public Object getRealValue() {
		return getRealValue(column, value);
	}

}

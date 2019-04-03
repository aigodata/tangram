package com.github.mengxianun.core.item;

import com.github.mengxianun.core.schema.Column;

public class ValueItem extends Item {

	private static final long serialVersionUID = 1L;
	private Column column;
	private Object value;

	public ValueItem(Column column, Object value) {
		this.column = column;
		this.value = value;
	}

	public Column getColumn() {
		return column;
	}

	public void setColumn(Column column) {
		this.column = column;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getRealValue() {
		return getRealValue(column, value);
	}

}

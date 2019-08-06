package com.github.mengxianun.core.schema;


public abstract class AbstractColumnType implements ColumnType {

	protected final String name;

	public AbstractColumnType(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isTime() {
		return false;
	}

}

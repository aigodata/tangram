package com.github.mengxianun.core.schema;


public interface ColumnType {

	public Integer getType();

	public String getName();

	public boolean isBoolean();

	public boolean isBinary();

	public boolean isNumber();

	public boolean isInteger();

	public boolean isDouble();

	public boolean isTimeBased();

	public boolean isDate();

	public boolean isTime();

	public boolean isTimestamp();

	public boolean isLiteral();

	public boolean isLargeObject();

	public boolean isJson();

}

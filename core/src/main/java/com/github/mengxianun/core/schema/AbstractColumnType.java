package com.github.mengxianun.core.schema;


public abstract class AbstractColumnType implements ColumnType {

	public static final String TYPE_INT = "int";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_DECIMAL = "decimal";
	public static final String TYPE_VARCHAR = "varchar";
	public static final String TYPE_BOOL = "bool";

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

	@Override
	public boolean isLiteralArray() {
		return isArray() && name.contains(TYPE_VARCHAR);
	}

	@Override
	public boolean isBooleanArray() {
		return isArray() && name.contains(TYPE_BOOL);
	}

	@Override
	public boolean isNumberArray() {
		return isIntArray() || isDoubleArray();
	}

	@Override
	public boolean isIntArray() {
		return isArray() && name.contains(TYPE_INT);
	}

	@Override
	public boolean isDoubleArray() {
		return isArray() && name.contains(TYPE_FLOAT) && name.contains(TYPE_DOUBLE) && name.contains(TYPE_DECIMAL);
	}

}

package com.github.mengxianun.elasticsearch.schema;

import com.github.mengxianun.core.schema.AbstractColumnType;

public class ElasticsearchColumnType extends AbstractColumnType {

	// simple type
	public static final String TEXT = "text";
	public static final String KEYWORD = "keyword";
	public static final String DATE = "date";
	public static final String LONG = "long";
	public static final String DOUBLE = "double";
	public static final String BOOLEAN = "boolean";
	public static final String IP = "ip";
	public static final String OBJECT = "object";

	public ElasticsearchColumnType(String name) {
		super(name);
	}

	@Override
	public boolean isBoolean() {
		return BOOLEAN.equals(name);
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return isInteger() || isLong() || isDouble();
	}

	@Override
	public boolean isInteger() {
		return false;
	}

	@Override
	public boolean isLong() {
		return LONG.equals(name);
	}

	@Override
	public boolean isDouble() {
		return DOUBLE.equals(name);
	}

	@Override
	public boolean isTimeBased() {
		return false;
	}

	@Override
	public boolean isDate() {
		return DATE.equals(name);
	}

	@Override
	public boolean isTime() {
		return false;
	}

	@Override
	public boolean isTimestamp() {
		return false;
	}

	@Override
	public boolean isLiteral() {
		return TEXT.equals(name) || KEYWORD.equals(name);
	}

	@Override
	public boolean isLargeObject() {
		return false;
	}

	@Override
	public boolean isJson() {
		return false;
	}

	public boolean isIP() {
		return IP.equals(name);
	}

	public boolean isObject() {
		return OBJECT.equals(name);
	}

	@Override
	public boolean isArray() {
		return false;
	}

}

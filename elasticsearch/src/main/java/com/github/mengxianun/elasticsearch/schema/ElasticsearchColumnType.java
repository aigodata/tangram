package com.github.mengxianun.elasticsearch.schema;

import com.github.mengxianun.core.schema.AbstractColumnType;

public class ElasticsearchColumnType extends AbstractColumnType {

	// simple type
	private static final String TEXT = "text";
	private static final String KEYWORD = "keyword";
	private static final String DATE = "date";
	private static final String LONG = "long";
	private static final String DOUBLE = "double";
	private static final String BOOLEAN = "boolean";
	private static final String IP = "ip";

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

}

package com.github.mengxianun.elasticsearch.schema;

import java.sql.Timestamp;

import com.github.mengxianun.core.schema.AbstractColumnType;
import com.google.common.base.Strings;

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

	public static final String DATE_FORMAT_EPOCH_MILLIS = "epoch_millis";

	private final String format;

	public ElasticsearchColumnType(String name) {
		this(name, null);
	}

	public ElasticsearchColumnType(String name, String format) {
		super(name);
		this.format = format;
	}

	public String getFormat() {
		return format;
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
	public Object getTimeValue(Object value) {
		Object timeValue = super.getTimeValue(value);
		if (!Strings.isNullOrEmpty(format)) {
			Timestamp timestamp = (Timestamp) timeValue;
			if (DATE_FORMAT_EPOCH_MILLIS.equals(format)) {
				return timestamp.getTime();
			}
		}
		return timeValue;
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

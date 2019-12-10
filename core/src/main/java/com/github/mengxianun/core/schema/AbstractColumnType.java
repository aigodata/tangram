package com.github.mengxianun.core.schema;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public abstract class AbstractColumnType implements ColumnType {

	public static final String TYPE_INT = "int";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_DOUBLE = "double";
	public static final String TYPE_DECIMAL = "decimal";
	public static final String TYPE_VARCHAR = "varchar";
	public static final String TYPE_BOOL = "bool";

	private static final Parser parser = new Parser();

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
		return isInteger() || isLong() || isDouble();
	}

	@Override
	public boolean isTimeBased() {
		return isDate() || isTime() || isTimestamp();
	}

	@Override
	public boolean isTime() {
		return false;
	}

	@Override
	public Object getTimeValue(Object value) {
		if (value == null) {
			return null;
		}
		List<DateGroup> groups = parser.parse(value.toString());
		if (groups.isEmpty()) {
			throw new DateTimeException(String.format("Unable to parse time format [%s]", value));
		}
		Date date = groups.get(0).getDates().get(0);
		return new Timestamp(date.getTime());
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

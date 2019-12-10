package com.github.mengxianun.jdbc.schema;

import java.sql.Types;

import com.github.mengxianun.core.schema.AbstractColumnType;

public class JdbcColumnType extends AbstractColumnType {

	private Integer type;

	public JdbcColumnType(Integer type, String name) {
		super(name);
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public String getTypeName() {
		return name.replaceFirst("^_", "");
	}

	@Override
	public boolean isBoolean() {
		return type == Types.BIT || type == Types.BOOLEAN;
	}

	@Override
	public boolean isBinary() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER || type == Types.BIGINT
				|| type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE || type == Types.NUMERIC
				|| type == Types.DECIMAL;
	}

	@Override
	public boolean isInteger() {
		return type == Types.TINYINT || type == Types.SMALLINT || type == Types.INTEGER || type == Types.BIGINT;
	}

	@Override
	public boolean isLong() {
		return false;
	}

	@Override
	public boolean isDouble() {
		return type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE;
	}

	@Override
	public boolean isDate() {
		return type == Types.DATE;
	}

	@Override
	public boolean isTime() {
		return type == Types.TIME || type == Types.TIME_WITH_TIMEZONE;
	}

	@Override
	public boolean isTimestamp() {
		return type == Types.TIMESTAMP || type == Types.TIMESTAMP_WITH_TIMEZONE;
	}

	@Override
	public boolean isLiteral() {
		return type == Types.CHAR || type == Types.VARCHAR || type == Types.LONGNVARCHAR || type == Types.CLOB
				|| type == Types.NCHAR || type == Types.NVARCHAR || type == Types.LONGNVARCHAR || type == Types.NCLOB;
	}

	@Override
	public boolean isLargeObject() {
		return false;
	}

	@Override
	public boolean isJson() {
		return type == Types.OTHER || "json".equals(name);
	}

	@Override
	public boolean isArray() {
		return type == Types.ARRAY;
	}

}

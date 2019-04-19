package com.github.mengxianun.core.schema;

import java.sql.Types;

public class DefaultColumnType implements ColumnType {

	private Integer type;
	private String name;

	public DefaultColumnType(Integer type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public Integer getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isBoolean() {
		return type == Types.BIT || type == Types.BOOLEAN;
	}

	@Override
	public boolean isBinary() {
		// TODO Auto-generated method stub
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
	public boolean isDouble() {
		return type == Types.FLOAT || type == Types.REAL || type == Types.DOUBLE;
	}

	@Override
	public boolean isTimeBased() {
		return isDate() || isTime() || isTimestamp();
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isJson() {
		return type == Types.OTHER || "json".equals(name);
	}

}

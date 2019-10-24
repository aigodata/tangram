package com.github.mengxianun.core.dialect;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.schema.ColumnType;

public abstract class AbstractDialect implements Dialect {

	private final Map<String, Function> functions;

	public AbstractDialect() {
		this(new HashMap<>());
	}

	public AbstractDialect(Map<String, Function> functions) {
		this.functions = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.functions.putAll(functions);
	}

	@Override
	public boolean hasFunction(String func) {
		return functions.containsKey(func);
	}

	@Override
	public Function getFunction(String func) {
		return functions.get(func);
	}

	@Override
	public String getWhereColumn(String column, ColumnType columnType, Operator operator) {
		if ((operator == Operator.LIKE || operator == Operator.NOT_LIKE) && columnType.isTimeBased()) {
			return getTimeLikeColumn(column);
		}
		return column;
	}

	protected String getTimeLikeColumn(String column) {
		return column;
	}

}

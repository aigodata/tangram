package com.github.mengxianun.core.parser.info.extension;

import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operator;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StatementValueConditionInfo {

	public static StatementValueConditionInfo create(ColumnInfo columnInfo, Operator operator, String statement) {
		return create(Connector.AND, columnInfo, operator, statement);
	}

	public static StatementValueConditionInfo create(Connector connector, ColumnInfo columnInfo, Operator operator,
			String statement) {
		return new AutoValue_StatementValueConditionInfo(connector, columnInfo, operator, statement);
	}

	public abstract Connector connector();

	public abstract ColumnInfo columnInfo();

	public abstract Operator operator();

	public abstract String statement();

}

package com.github.mengxianun.core.parser.info.extension;

import com.github.mengxianun.core.request.Connector;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class StatementConditionInfo {

	public static StatementConditionInfo create(String statement) {
		return create(Connector.AND, statement);
	}

	public static StatementConditionInfo create(Connector connector, String statement) {
		return new AutoValue_StatementConditionInfo(connector, statement);
	}

	public abstract Connector connector();

	public abstract String statement();

}

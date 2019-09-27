package com.github.mengxianun.core.parser.info;

import com.github.mengxianun.core.request.Connector;
import com.google.auto.value.AutoValue;

@Deprecated
@AutoValue
public abstract class StatementFilterInfo {

	public static StatementFilterInfo create(ConditionInfo conditionInfo) {
		return new AutoValue_StatementFilterInfo(Connector.AND, conditionInfo);
	}

	public static StatementFilterInfo create(Connector connector, ConditionInfo conditionInfo) {
		return new AutoValue_StatementFilterInfo(connector, conditionInfo);
	}

	public abstract Connector connector();

	public abstract ConditionInfo conditionInfo();

}

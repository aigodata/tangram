package com.github.mengxianun.core.parser.info;

import com.github.mengxianun.core.request.Operator;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConditionInfo {

	public static ConditionInfo create(ColumnInfo columnInfo, Operator operator, Object value) {
		return new AutoValue_ConditionInfo(columnInfo, operator, value);
	}

	public abstract ColumnInfo columnInfo();

	public abstract Operator operator();

	public abstract Object value();

}

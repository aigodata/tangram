package com.github.mengxianun.core.parser.info;

import javax.annotation.Nullable;

import com.github.mengxianun.core.request.Operator;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConditionInfo {

	public static ConditionInfo create(ColumnInfo columnInfo, Operator operator, @Nullable Object value) {
		return new AutoValue_ConditionInfo(columnInfo, operator, value);
	}

	public abstract ColumnInfo columnInfo();

	public abstract Operator operator();

	@Nullable
	public abstract Object value();

}

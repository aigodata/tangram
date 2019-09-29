package com.github.mengxianun.core.permission;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnCondition implements Condition {

	public static ColumnCondition create(String table, String column, Object value) {
		return create(null, table, column, value);
	}

	public static ColumnCondition create(@Nullable String source, String table, String column, Object value) {
		return new AutoValue_ColumnCondition(source, table, column, value);
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	public abstract String column();

	public abstract Object value();

}

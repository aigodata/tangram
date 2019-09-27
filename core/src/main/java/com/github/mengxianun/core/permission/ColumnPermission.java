package com.github.mengxianun.core.permission;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnPermission {

	public static ColumnPermission create(String table, String column, ColumnAction action) {
		return create(null, table, column, action);
	}

	public static ColumnPermission create(@Nullable String source, String table, String column, ColumnAction action) {
		return new AutoValue_ColumnPermission(source, table, column, action);
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	public abstract String column();

	public abstract ColumnAction action();

}

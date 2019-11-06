package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnPermission {

	public static ColumnPermission create(String table, String column, Action action) {
		return create(null, table, column, action, Collections.emptyList());
	}

	public static ColumnPermission create(@Nullable String source, String table, String column, Action action) {
		return create(source, table, column, action, Collections.emptyList());
	}

	public static ColumnPermission create(@Nullable String source, String table, String column, Action action,
			List<ConnectorCondition> conditions) {
		return new AutoValue_ColumnPermission(source, table, column, action, conditions);
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	public abstract String column();

	public abstract Action action();

	public abstract List<ConnectorCondition> conditions();

}

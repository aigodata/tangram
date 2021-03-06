package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TablePermission {

	public static TablePermission create(String table) {
		return create(null, table);
	}

	public static TablePermission create(@Nullable String source, String table) {
		return create(source, table, Action.ALL);
	}

	public static TablePermission create(@Nullable String source, String table, Action action) {
		return create(source, table, null, action, Collections.emptyList());
	}

	public static TablePermission create(@Nullable String source, String table, @Nullable Object id, Action action,
			List<ConnectorCondition> conditions) {
		return new AutoValue_TablePermission.Builder().source(source).table(table).id(id).action(action)
				.conditions(conditions).build();
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	@Nullable
	public abstract Object id();

	public abstract Action action();

	public abstract List<ConnectorCondition> conditions();

	public static Builder builder() {
		return new AutoValue_TablePermission.Builder().conditions(Collections.emptyList());
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder source(String source);

		public abstract Builder table(String table);

		public abstract Builder id(Object id);

		public abstract Builder action(Action action);

		public abstract Builder conditions(List<ConnectorCondition> conditions);

		public abstract TablePermission build();
	}

}

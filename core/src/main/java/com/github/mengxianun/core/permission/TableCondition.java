package com.github.mengxianun.core.permission;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TableCondition implements Condition {

	public static TableCondition create(String table) {
		return create(table, null, null);
	}

	public static TableCondition create(String table, String column) {
		return create(table, column, null);
	}

	public static TableCondition create(String table, String column, @Nullable Object value) {
		return create(null, table, column, value);
	}

	public static TableCondition create(@Nullable String source, String table, String column,
			@Nullable Object value) {
		return new AutoValue_TableCondition.Builder().source(source).table(table).column(column).value(value).build();
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	public abstract String column();

	@Nullable
	public abstract Object value();

	public static Builder builder() {
		return new AutoValue_TableCondition.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder source(String source);

		public abstract Builder table(String table);

		public abstract Builder column(String column);

		public abstract Builder value(Object value);

		public abstract TableCondition build();
	}

}

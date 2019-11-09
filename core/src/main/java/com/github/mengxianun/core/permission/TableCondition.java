package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TableCondition implements Condition {

	public static TableCondition create(String table, String column, Object value) {
		return create(null, table, column, value);
	}

	public static TableCondition create(@Nullable String source, String table, String column, Object value) {
		return create(source, table, column, value, Collections.emptyList());
	}

	public static TableCondition create(@Nullable String source, String table, String column, Object value,
			List<String> relationColumns) {
		return new AutoValue_TableCondition.Builder().source(source).table(table).column(column).value(value)
				.relationColumns(relationColumns).build();
	}

	@Nullable
	public abstract String source();

	public abstract String table();

	public abstract String column();

	public abstract Object value();

	/**
	 * There are multiple columns in the daa table that are associated with the
	 * condition table. This property specifies the columns of the data table.
	 * 
	 * @return
	 */
	public abstract List<String> relationColumns();

	public static Builder builder() {
		return new AutoValue_TableCondition.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder source(String source);

		public abstract Builder table(String table);

		public abstract Builder column(String column);

		public abstract Builder value(Object value);

		public abstract Builder relationColumns(List<String> relationColumns);

		public abstract TableCondition build();
	}

}

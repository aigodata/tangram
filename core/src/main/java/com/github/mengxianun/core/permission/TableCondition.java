package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.mengxianun.core.parser.info.RelationInfo;
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
			List<RelationInfo> relations) {
		return create(source, table, column, value, relations, Collections.emptyList());
	}

	public static TableCondition create(@Nullable String source, String table, String column, Object value,
			List<RelationInfo> relations, List<String> relationTablesPath) {
		return new AutoValue_TableCondition.Builder().source(source).table(table).column(column).value(value)
				.relations(relations).relationTablesPath(relationTablesPath).build();
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
	public abstract List<RelationInfo> relations();

	/**
	 * 
	 * @return
	 */
	public abstract List<String> relationTablesPath();

	public static Builder builder() {
		return new AutoValue_TableCondition.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder source(String source);

		public abstract Builder table(String table);

		public abstract Builder column(String column);

		public abstract Builder value(Object value);

		public abstract Builder relations(List<RelationInfo> relations);

		public abstract Builder relationTablesPath(List<String> relationTablesPath);

		public abstract TableCondition build();
	}

}

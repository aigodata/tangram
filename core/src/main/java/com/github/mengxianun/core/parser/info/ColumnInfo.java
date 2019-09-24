package com.github.mengxianun.core.parser.info;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnInfo {

	public static ColumnInfo create(@Nullable String source, @Nullable String table, String column, String alias) {
		return new AutoValue_ColumnInfo(source, table, column, alias);
	}

	@Nullable
	public abstract String source();

	@Nullable
	public abstract String table();

	@Nullable
	public abstract String column();

	@Nullable
	public abstract String alias();

}

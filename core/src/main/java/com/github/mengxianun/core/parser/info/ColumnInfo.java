package com.github.mengxianun.core.parser.info;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnInfo {

	public static ColumnInfo create(@Nullable String source, @Nullable String table, String column, String alias) {
		return create(source, table, column, alias, null);
	}

	public static ColumnInfo create(@Nullable String source, @Nullable String table, String column, String alias,
			@Nullable String origin) {
		return new AutoValue_ColumnInfo(source, table, column, alias, origin);
	}

	@Nullable
	public abstract String source();

	@Nullable
	public abstract String table();

	@Nullable
	public abstract String column();

	@Nullable
	public abstract String alias();

	/**
	 * The original value. (optimize!)
	 * 
	 * @return
	 */
	@Nullable
	public abstract String origin();

}

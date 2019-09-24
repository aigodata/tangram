package com.github.mengxianun.core.parser.info;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TableInfo {

	public static TableInfo create(@Nullable String source, @Nullable String table, @Nullable String alias) {
		return new AutoValue_TableInfo(source, table, alias);
	}

	@Nullable
	public abstract String source();

	@Nullable
	public abstract String table();

	@Nullable
	public abstract String alias();

}

package com.github.mengxianun.core.permission;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TableKey {

	static TableKey create(@Nullable String source, String table) {
		return new AutoValue_TableKey(source, table);
	}

	@Nullable
	abstract String source();

	abstract String table();

}

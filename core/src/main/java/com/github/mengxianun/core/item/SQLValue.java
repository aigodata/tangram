package com.github.mengxianun.core.item;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SQLValue {

	public static SQLValue create(String sql) {
		return new AutoValue_SQLValue(sql);
	}

	public abstract String sql();

}

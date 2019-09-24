package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SqlInfo {

	public static SqlInfo create(String sql) {
		return new AutoValue_SqlInfo(sql);
	}

	public abstract String sql();

}
package com.github.mengxianun.core.parser.info;

import java.util.List;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class WhereInfo {

	public static WhereInfo create(List<FilterInfo> filters) {
		return new AutoValue_WhereInfo(filters);
	}

	public abstract List<FilterInfo> filters();

}

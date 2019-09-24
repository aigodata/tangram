package com.github.mengxianun.core.parser.info;

import java.util.Map;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ValuesInfo {

	public static ValuesInfo create(Map<String, Object> values) {
		return new AutoValue_ValuesInfo(values);
	}

	public abstract Map<String, Object> values();

}

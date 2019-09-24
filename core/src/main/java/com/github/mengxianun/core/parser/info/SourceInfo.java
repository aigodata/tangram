package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SourceInfo {

	public static SourceInfo create(String source) {
		return new AutoValue_SourceInfo(source);
	}

	public abstract String source();

}

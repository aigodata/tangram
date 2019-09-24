package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class NativeInfo {

	public static NativeInfo create(String content) {
		return new AutoValue_NativeInfo(content);
	}

	public abstract String content();

}

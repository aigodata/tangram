package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class LimitInfo {

	public static LimitInfo create(long start, long end) {
		return new AutoValue_LimitInfo(start, end);
	}

	public abstract long start();

	public abstract long end();

}

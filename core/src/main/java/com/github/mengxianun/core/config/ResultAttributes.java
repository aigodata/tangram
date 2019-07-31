package com.github.mengxianun.core.config;

public final class ResultAttributes {

	private ResultAttributes() {
		throw new IllegalStateException("Utility class");
	}

	public static final String PRIMARY_KEY = "primary_key";
	public static final String COUNT = "count";
	// 分页相关
	public static final String DATA = "data";
	public static final String START = "start";
	public static final String END = "end";
	public static final String TOTAL = "total";

}

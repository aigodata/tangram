package com.github.mengxianun.core.config;


public final class ColumnConfig {

	private ColumnConfig() {
		throw new IllegalStateException("Utility class");
	}

	public static final String NAME = "name";
	// 别名
	public static final String ALIAS = "alias";
	// 日期时间字段返回格式
	public static final String TIME_FORMAT = "time_format";

}

package com.github.mengxianun.core.config;

public final class TableConfig {

	private TableConfig() {
		throw new IllegalStateException("Utility class");
	}

	// 列
	public static final String COLUMNS = "columns";
	// 列显示
	public static final String COLUMN_DISPLAY = "display";
	// 列格式
	public static final String COLUMN_FORMAT = "format";
	// 列关联
	public static final String COLUMN_ASSOCIATION = "association";
	public static final String ASSOCIATION_TARGET_TABLE = "target_table";
	public static final String ASSOCIATION_TARGET_COLUMN = "target_column";
	public static final String ASSOCIATION_TYPE = "type";
	// 列忽略, 返回数据不包含该列
	public static final String COLUMN_IGNORE = "ignore";
	// JSON 节点的 key 名称
	public static final String JSON_KEY = "json_key";

}

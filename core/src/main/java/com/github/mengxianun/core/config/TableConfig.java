package com.github.mengxianun.core.config;

public interface TableConfig {

	// 列
	String COLUMNS = "columns";
	// 列显示
	String COLUMN_DISPLAY = "display";
	// 列格式
	String COLUMN_FORMAT = "format";
	// 列关联
	String COLUMN_ASSOCIATION = "association";
	String ASSOCIATION_TARGET_TABLE = "target_table";
	String ASSOCIATION_TARGET_COLUMN = "target_column";
	String ASSOCIATION_TYPE = "type";
	// 列忽略, 返回数据不包含该列
	String COLUMN_IGNORE = "ignore";
	// JSON 节点的 key 名称
	String JSON_KEY = "json_key";

}

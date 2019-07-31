package com.github.mengxianun.core.config;

public final class GlobalConfig {

	private GlobalConfig() {
		throw new IllegalStateException("Utility class");
	}

	// 配置文件
	public static final String CONFIG_FILE = "config_file";
	// 数据源
	public static final String DATASOURCES = "datasources";
	// 如果数据不存在, 就新增一条
	public static final String UPSERT = "upsert";
	// 是否启用原生语句, 默认false
	public static final String NATIVE = "native";
	// 默认数据源
	public static final String DEFAULT_DATASOURCE = "default_datasource";
	// 表信息配置文件路径
	public static final String TABLE_CONFIG_PATH = "table_config_path";
	// 所有数据库表配置, 该属性项非配置文件配置, 是项目自动生成的属性, 目的是将数据库表的配置信息与项目全局的配置信息放在一个对象里 全局配置
	public static final String TABLE_CONFIG = "table_config";


}

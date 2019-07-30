package com.github.mengxianun.core.config;

public interface GlobalConfig {

	// 配置文件
	String CONFIG_FILE = "config_file";
	// 数据源
	String DATASOURCES = "datasources";
	// 数据源类型
	String DATASOURCE_TYPE = "type";
	// 如果数据不存在, 就新增一条
	String UPSERT = "upsert";
	// 是否启用原生语句, 默认false
	String NATIVE = "native";
	// 是否启用日志
	String LOG = "log";
	// 默认数据源
	String DEFAULT_DATASOURCE = "default_datasource";
	// 表信息配置文件路径
	String TABLE_CONFIG_PATH = "table_config_path";
	// 所有数据库表配置, 该属性项非配置文件配置, 是项目自动生成的属性, 目的是将数据库表的配置信息与项目全局的配置信息放在一个对象里 全局配置
	String TABLE_CONFIG = "table_config";
	// 预处理开关
	String PRE_HANDLER = "pre_handler";
	// 权限关联
	String AUTH_CONTROL = "auth_control";


}

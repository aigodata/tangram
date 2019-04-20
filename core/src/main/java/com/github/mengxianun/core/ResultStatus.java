package com.github.mengxianun.core;

/**
 * 结果状态
 * 
 * @author mengxiangyun
 *
 */
public enum ResultStatus {

	// 成功
	SUCCESS(0, "ok"),

	/**
	 * 权限错误
	 */
	AUTHENTICATION(10100, "You don't have permission to access the table"),

	/**
	 * 数据源错误
	 */
	// -- 预留
	DATASOURCE(10200, ""),
	// 数据源不存在
	DATASOURCE_NOT_EXIST(10201, "Data source [%s] does not exist"),
	// 数据库表不存在
	DATASOURCE_TABLE_NOT_EXIST(10202, "Database tables [%s] do not exist"),
	// 列不存在
	DATASOURCE_COLUMN_NOT_EXIST(10203, "Column [%s] do not exist"),
	// SQL 执行失败
	DATASOURCE_SQL_FAILED(10204, "SQL statement execution failed. %s"),
	// 关联关系不存在
	DATASOURCE_RELATIONSHIP_NOT_FOUND(10205, "The association relation of [%s] table is not found"),
	// 其他操作异常
	DATASOURCE_EXCEPTION(10210, "%s"),

	/**
	 * JSON 错误
	 */
	//
	JSON(10400, ""),
	// 不支持的属性
	JSON_UNSUPPORTED_ATTRIBUTE(10401, "Unsupported attributes [%s]"),
	// JSON 属性格式错误
	JSON_ATTRIBUTE_FORMAT_ERROR(10402, "Json attribute [%s] format error, %s."),
	// JSON 解析失败
	JSON_FORMAT_ERROR(10403, "JSON format error."),

	/*
	 * 其他错误
	 */
	// 翻译失败
	TRANSLATION_FAILED(10800, "Json translation failed"),
	// 原生语句执行失败
	NATIVE_FAILED(10801, "Native statement execution failed"),
	// 资源关闭失败
	RESOURCE_DESTROY_FAILED(10801, "Resource [%s] destroy failed"),
	
	/*
	 * 系统错误
	 */
	SYSTEM_ERROR(10900, "System error: %s");

	// 状态码
	private int code;
	// 消息
	private String message;

	ResultStatus(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int code() {
		return this.code;
	}

	public String message() {
		return this.message;
	}

	public String fill(Object... args) {
		return String.format(this.message, args);
	}

}

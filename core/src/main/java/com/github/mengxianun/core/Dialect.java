package com.github.mengxianun.core;

import com.github.mengxianun.core.dialect.Function;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.schema.ColumnType;
import com.google.gson.JsonElement;

public interface Dialect {

	public String getType();

	default String getIdentifierQuoteString() {
		return "";
	}

	/**
	 * 是否指定数据库. 例: select database.table
	 * 
	 * @return 是否指定数据库
	 */
	default boolean schemaPrefix() {
		return true;
	}

	/**
	 * 是否验证数据库表是否存在
	 * 
	 * @return 是否验证数据库表是否存在
	 */
	default boolean validTableExists() {
		return true;
	}

	/**
	 * 处理关键字
	 * 
	 * @param keyword
	 * @return 处理后的关键字
	 */
	default String processKeyword(String keyword) {
		return keyword;
	}

	/**
	 * 是否启用表别名
	 * 
	 * @return 是否启用表别名
	 */
	default boolean tableAliasEnabled() {
		return true;
	}

	/**
	 * 是否启用列别名
	 * 
	 * @return 是否启用列别名
	 */
	default boolean columnAliasEnabled() {
		return true;
	}

	/**
	 * 在表或列未指定别名的情况下, 是否生成随机别名
	 * 
	 * @return 是否生成随机别名
	 */
	default boolean randomAliasEnabled() {
		return true;
	}

	/**
	 * 获取 Json 占位符
	 * 
	 * @return Json 占位符
	 */
	default String getJsonPlaceholder() {
		return "?";
	}
	
	/**
	 * 解析数据源返回的 json 列值
	 * 
	 * @param value
	 * @return
	 */
	default JsonElement getJsonValue(Object value) {
		return App.gson().toJsonTree(value);
	}

	/**
	 * 数据库记录起始位置, 默认从1开始
	 * 
	 * @return 数据库记录起始位置
	 */
	default long offset() {
		return 1;
	}

	public boolean hasFunction(String func);

	public Function getFunction(String func);

	public String getWhereColumn(String column, ColumnType columnType, Operator operator);

}

package com.github.mengxianun.core;

public interface Dialect {

	public String getType();

	default Class<? extends SQLBuilder> getSQLBuilder() {
		return SQLBuilder.class;
	}

	/**
	 * 是否指定数据库. 例: select database.table
	 * 
	 * @return 是否指定数据库
	 */
	default boolean assignDatabase() {
		return true;
	}

	/**
	 * 验证数据库表是否存在
	 * 
	 * @return 数据库表是否存在
	 */
	default boolean validTableExists() {
		return true;
	}

	/**
	 * 是否用引用符号包裹表和列. 例: select "column" from "table". 引用符号因数据库不同而不同, MySQL 为 '`'
	 * 
	 * @return 是否用引用符号包裹表和列
	 */
	default boolean quoteTable() {
		return true;
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

}

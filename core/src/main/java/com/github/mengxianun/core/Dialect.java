package com.github.mengxianun.core;

public interface Dialect {

	public String getType();

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
	 * @return
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
	 * 分页起始
	 * 
	 * @return
	 */
	default long offset() {
		return 1;
	}

}

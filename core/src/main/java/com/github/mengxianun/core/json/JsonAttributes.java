package com.github.mengxianun.core.json;

public interface JsonAttributes {
	
	// 类型
	public static final String TYPE = "type";
	// 字段
	public static final String FIELDS = "fields";
	// 字段别名关键字
	public static final String ALIAS_KEY = " as ";
	// 关联表
	public static final String JOIN = "join";
	// insert 或 update 的值
	public static final String VALUES = "values";
	// where
	public static final String WHERE = "where";
	// group
	public static final String GROUP = "group";
	// order
	public static final String ORDER = "order";
	// limit
	public static final String LIMIT = "limit";
	// 原生语句
	public static final String NATIVE = "native";
	// 数据源
	public static final String SOURCE = "source";
	// 结果类型
	public static final String RESULT = "result";
	// 模板
	public static final String TEMPLATE = "template";
	/*
	 * 操作类型
	 */
	public static final String DETAIL = "detail";
	public static final String QUERY = "query";
	public static final String SELECT = "select";
	public static final String INSERT = "insert";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	// 事务
	public static final String TRANSACTION = "transaction";
	// 配置结构信息
	public static final String STRUCT = "struct";

}

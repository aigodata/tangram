package com.github.mengxianun.core;

import java.util.List;
import java.util.Set;

import com.github.mengxianun.core.attributes.AssociationType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.JsonElement;

public interface DataContext {

	public JsonElement action(Action action);

	public JsonElement action(Action... actions);

	public JsonElement executeNative(String script);

	public JsonElement executeNative(Table table, String script);

	public List<Schema> getSchemas();

	public Schema getDefaultSchema();

	public Schema getSchema(String schemaName);

	public Table getTable(String tableName);

	public Table getTable(String schemaName, String tableName);

	public Column getColumn(String tableName, String columnName);

	public Column getColumn(String schemaName, String tableName, String columnName);

	public String getIdentifierQuoteString();

	public Dialect getDialect();

	public void destroy() throws Throwable;

	public void addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType);

	/**
	 * 获取主外表的关联关系.
	 * <li>如 A join B, 获取 A 对 B 的关联关系, 将会得到 [ A-B ]
	 * <li>如 A join B join C, 获取 A 对 C 的关联关系, 将会得到 [ A-B, B-C ]
	 * <li>如果没有找到关联关系, 返回一个空集合
	 * 
	 * @param primaryTable
	 * @param foreignTable
	 * @return 主表到外表的多级关系
	 */
	public Set<Relationship> getRelationships(Table primaryTable, Table foreignTable);

	/**
	 * 获取2个表的关联关系类型. 多层关联的情况下, 以收尾的关系为最终关系. 如 A 一对多 B, B 多对一 C, 则 A 一对一 C
	 * 
	 * @param primaryTable
	 * @param foreignTable
	 * @return AssociationType
	 */
	public AssociationType getAssociationType(Table primaryTable, Table foreignTable);

}

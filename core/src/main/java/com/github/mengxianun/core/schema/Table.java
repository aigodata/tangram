package com.github.mengxianun.core.schema;

import java.util.List;

import com.github.mengxianun.core.attributes.AssociationType;
import com.google.gson.JsonObject;

public interface Table extends Name {

	public Schema getSchema();

	public int getColumnCount();

	public List<Column> getColumns();

	public List<String> getColumnNames();

	public Column getColumnByName(String columnName);

	public List<Column> getPrimaryKeys();

	public String getRemarks();

	/**
	 * 获取配置的主表直接关联的所有表关联关系
	 * 
	 * @return
	 */
	public List<Relationship> getRelationships();

	/**
	 * 获取配置的主表的直接关联的关联关系
	 * 
	 * @param foreignTable
	 * @return
	 */
	public Relationship getRelationship(Table foreignTable);

	/**
	 * 获取配置的多层关联关系. 如 A join B join C, 获取 A 对 C 的关联关系, 将会得到 [ A-B, B-C]
	 * 
	 * @param foreignTable
	 * @return
	 */
	public List<Relationship> getCrossRelationships(Table foreignTable);



	/**
	 * 获取关联关系
	 * 
	 * @param foreignTable
	 * @return
	 */
	public AssociationType getAssociationType(Table foreignTable);

	public JsonObject getConfig();

	default void setConfig(JsonObject config) {
	}

}

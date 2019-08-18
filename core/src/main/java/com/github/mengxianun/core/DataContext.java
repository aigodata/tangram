package com.github.mengxianun.core;

import java.util.List;
import java.util.Set;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.MultiSummary;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.RelationshipPath;

public interface DataContext {

	public Summary execute(Action action);

	public MultiSummary execute(Action... actions);

	public Summary executeSql(String sql);

	public Summary executeNative(String statement);

	public List<Schema> getSchemas();

	public Schema getDefaultSchema();

	public Schema getSchema(String schemaName);

	/**
	 * 根据表名查询数据表
	 * 
	 * @param tableName
	 *            Config中配置的表别名或者实际的表名
	 * @return Table
	 */
	public Table getTable(String tableName);

	public Table getTable(String schemaName, String tableName);

	public Column getColumn(String tableName, String columnName);

	public Column getColumn(String schemaName, String tableName, String columnName);

	public Dialect getDialect();

	public SQLBuilder getSQLBuilder(Action action);

	public void destroy();

	public void addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType);

	/**
	 * 获取2个表的关联关系, 包含所有可能的路径
	 * 
	 * <pre>
	 * <li>TableA  TableB  TableC  TableD
	 * <li>Column1 Column1
	 * <li>Column2 Column1
	 * <li>Column3         Column1
	 * <li>Column4                 Column1
	 * <li>        Column2 Column1
	 * <li>        Column3 Column1
	 * <li>                Column2 Column1
	 * </pre>
	 * 
	 * <li>A-B的关系: [[A.C1-B.C1], [A.C2-B.C1]]
	 * <li>A-C的关系: [[A.C1-B.C1, B.C2-C.C1], [A.C1-B.C1, A.C3-B.C1], [A.C2-B.C1,
	 * B.C2-C.C1], [A.C2-B.C1, B.C3-C.C1]]
	 * <li>B-C的关系: [[B.C2-C.C1, B.C3-C.C1]]
	 * <li>C-D的关系: [[C.C2-D.C1]]
	 * <li>
	 * 
	 * @param primaryTable
	 * @param foreignTable
	 * @return All relation paths
	 */
	public Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable);

	/**
	 * 获取2个表的关联关系类型. 多层关联的情况下, 以首尾的关系为最终关系. 如 A 一对多 B, B 多对一 C, 则 A 一对一 C
	 * 
	 * @param primaryTable
	 * @param foreignTable
	 * @return AssociationType
	 */
	public AssociationType getAssociationType(Table primaryTable, Table foreignTable);

}

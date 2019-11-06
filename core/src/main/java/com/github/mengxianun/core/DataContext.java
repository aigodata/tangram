package com.github.mengxianun.core;

import java.util.Set;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.MultiSummary;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.Relationship;
import com.github.mengxianun.core.schema.relationship.RelationshipPath;

public interface DataContext {

	public Summary execute(NewAction action);

	public MultiSummary execute(NewAction... actions);

	public Summary executeSql(String sql);

	public Summary executeNative(String statement);

	public Schema getSchema();

	public Schema loadSchema();

	/**
	 * Get the table by table name and table alias
	 * 
	 * @param nameOrAlias
	 * @return Table
	 */
	public Table getTable(String nameOrAlias);

	/**
	 * Get the table by table name
	 * 
	 * @param name
	 * @return Table
	 */
	public Table loadTable(String name);

	/**
	 * Get columns by table name or table alias and column name or column alias
	 * 
	 * @param tableNameOrAlias
	 * @param columnNameOrAlias
	 * @return Column
	 */
	public Column getColumn(String tableNameOrAlias, String columnNameOrAlias);

	public Dialect getDialect();

	public SQLBuilder getSQLBuilder(Action action);

	public void destroy();

	public boolean addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType);

	public boolean deleteRelationship(Column primaryColumn, Column foreignColumn);

	public boolean deleteRelationship(Table primaryTable, Table foreignTable);

	public void cleanRelationshipCache();

	public Set<Relationship> getAllRelationships();

	public Set<RelationshipPath> getRelationships(String primaryTable, String foreignTable);

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
	 * 获取2个表的关联关系类型. 多层关联的情况下, 以多的一端为最终关系. 如 A 一对多 B, B 多对一 C, 则 A 一对多 C
	 * 
	 * @param primaryTable
	 * @param foreignTable
	 * @return AssociationType
	 */
	public AssociationType getAssociationType(Table primaryTable, Table foreignTable);

	/**
	 * Refresh schema metadata
	 */
	public void refresh();

	/**
	 * Refresh table metadata
	 * 
	 * @param name
	 */
	public void refreshTable(String name);

}

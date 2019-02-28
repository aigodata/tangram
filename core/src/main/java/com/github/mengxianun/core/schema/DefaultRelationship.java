package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.attributes.AssociationType;

public class DefaultRelationship implements Relationship {

	private List<Column> primaryColumns = new ArrayList<>();
	private List<Column> foreignColumns = new ArrayList<>();
	private AssociationType associationType;

	public DefaultRelationship(List<Column> primaryColumns, List<Column> foreignColumns) {
		this.primaryColumns = primaryColumns;
		this.foreignColumns = foreignColumns;
		this.associationType = AssociationType.ONE_TO_ONE;
	}

	public DefaultRelationship(List<Column> primaryColumns, List<Column> foreignColumns,
			AssociationType associationType) {
		this.primaryColumns = primaryColumns;
		this.foreignColumns = foreignColumns;
		this.associationType = associationType;
	}

	public DefaultRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		this.primaryColumns.add(primaryColumn);
		this.foreignColumns.add(foreignColumn);
		this.associationType = associationType;
	}

	@Override
	public Table getPrimaryTable() {
		return primaryColumns.get(0).getTable();
	}

	@Override
	public List<Column> getPrimaryColumns() {
		return primaryColumns;
	}

	@Override
	public Table getForeignTable() {
		return foreignColumns.get(0).getTable();
	}

	@Override
	public List<Column> getForeignColumns() {
		return foreignColumns;
	}

	@Override
	public AssociationType getAssociationType() {
		return associationType;
	}

	@Override
	public void addRelation(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		this.primaryColumns.add(primaryColumn);
		this.foreignColumns.add(foreignColumn);
		this.associationType = associationType;
	}

	public void setPrimaryColumns(List<Column> primaryColumns) {
		this.primaryColumns = primaryColumns;
	}

	public void setForeignColumns(List<Column> foreignColumns) {
		this.foreignColumns = foreignColumns;
	}

	public void setAssociationType(AssociationType associationType) {
		this.associationType = associationType;
	}

}

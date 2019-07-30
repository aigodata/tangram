package com.github.mengxianun.core.item;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;

public class JoinColumnItem extends ColumnItem {

	private static final long serialVersionUID = 1L;
	private AssociationType associationType;
	// 关联的所有父表
	private List<Table> parentTables = new ArrayList<>();

	public JoinColumnItem(Column column) {
		super(column);
	}

	public JoinColumnItem(Column column, String alias, boolean customAlias) {
		super(column, alias, customAlias);
	}

	public JoinColumnItem(Column column, TableItem tableItem) {
		super(column, tableItem);
	}

	public JoinColumnItem(Column column, String alias, boolean customAlias, TableItem tableItem) {
		super(column, alias, customAlias, tableItem);
	}

	public JoinColumnItem(String expression) {
		super(expression);
	}

	public JoinColumnItem(String expression, String alias, boolean customAlias) {
		super(expression, alias, customAlias);
	}

	public void addParentTable(Table parentTable) {
		parentTables.add(parentTable);
	}

	public AssociationType getAssociationType() {
		return associationType;
	}

	public void setAssociationType(AssociationType associationType) {
		this.associationType = associationType;
	}

	public List<Table> getParentTables() {
		return parentTables;
	}

	public void setParentTables(List<Table> parentTables) {
		this.parentTables = parentTables;
	}

}

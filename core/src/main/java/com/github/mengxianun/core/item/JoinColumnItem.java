package com.github.mengxianun.core.item;

import com.github.mengxianun.core.schema.Column;

public class JoinColumnItem extends ColumnItem {

	private static final long serialVersionUID = 1L;

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

}

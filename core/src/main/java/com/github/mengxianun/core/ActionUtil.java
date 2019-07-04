package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;

public class ActionUtil {

	private ActionUtil() {
		throw new IllegalStateException("Utility class");
	}

	public static TableItem createTableItem(Table table) {
		return new TableItem(table, createTableAlias(table, true), false);
	}
	
	public static TableItem createTableItem(Table table, boolean isAlias) {
		return new TableItem(table, createTableAlias(table, isAlias), false);
	}

	public static TableItem createTableItem(Table table, String alias) {
		return new TableItem(table, alias, true);
	}

	public static List<ColumnItem> createColumnItems(TableItem tableItem, boolean isAlias) {
		List<ColumnItem> columnItems = new ArrayList<>();
		Table table = tableItem.getTable();
		String expression = tableItem.getExpression();
		if (table != null) {
			List<Column> columns = table.getColumns();
			for (Column column : columns) {
				columnItems.add(new ColumnItem(column, isAlias ? createRandomAlias() : null, false, tableItem));
			}
		} else if (!Strings.isNullOrEmpty(expression)) {
			columnItems.add(new ColumnItem(SQLBuilder.COLUMN_ALL));
		}
		return columnItems;
	}

	public static String createTableAlias(Table table) {
		return createTableAlias(table, true);
	}

	public static String createTableAlias(Table table, boolean isAlias) {
		String alias = null;
		if (isAlias) {
			alias = createRandomAlias();
			if (table != null) {
				alias += "_" + table.getName();
			}
		}
		return alias;
	}

	public static String createRandomAlias() {
		return createRandomString(6);
	}

	public static String createRandomString(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

}

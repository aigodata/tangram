package com.github.mengxianun.elasticsearch;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.SQLBuilder;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;

public class ElasticsearchSQLBuilder extends SQLBuilder {

	public ElasticsearchSQLBuilder(Action action) {
		super(action);
		if (action.isJoin()) {
			throw new UnsupportedOperationException("Elasticsearch join operation is not supported");
		}
	}

	public void toSelectWithoutLimit() {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(toColumns());
		sqlBuilder.append(toSelectTables());
		sqlBuilder.append(toJoins());
		sqlBuilder.append(toWhere());
		sqlBuilder.append(toGroups());
		sqlBuilder.append(toOrders());
		sql = sqlBuilder.toString();
	}

	@Override
	public String toLimit() {
		if (joinLimit) {
			return "";
		}
		LimitItem limitItem = action.getLimitItem();
		if (limitItem == null) {
			return "";
		}
		limitString = LIMIT + "?";
		params.add(limitItem.getLimit());
		// Elasticsearch SQL (6.8.2) 暂不支持 OFFSET
		return limitString;
	}

	@Override
	public String countSql() {
		// 主表列
		StringBuilder columnsBuilder = new StringBuilder();
		TableItem mainTableItem = action.getTableItems().get(0);
		Table mainTable = mainTableItem.getTable();
		String tablePrefix = Strings.isNullOrEmpty(mainTableItem.getAlias()) ? mainTable.getName()
				: mainTableItem.getAlias();
		boolean comma = false;
		for (Column column : mainTable.getColumns()) {
			if (column.getType().isJson()) { // 跳过 JSON 类型, 无法与 Distinct 一起使用
				continue;
			}
			if (comma) {
				columnsBuilder.append(", ");
			}
			if (dialect.tableAliasEnabled()) {
				columnsBuilder.append(tablePrefix).append(".");
			}
			columnsBuilder.append(column.getName());
			comma = true;
		}

		StringBuilder countBuilder = new StringBuilder();
		// 原始SQL
		// 1. 只查询主表的列
		// 2. 去掉 LIMIT 条件
		countBuilder.append(PREFIX_SELECT).append(COUNT).append(ALIAS_KEY).append("count").append(" ")
				.append(tableString).append(whereString).append(groupString);
		return countBuilder.toString();
	}

}

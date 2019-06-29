package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.item.GroupItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.OrderItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.item.ValueItem;
import com.github.mengxianun.core.json.Connector;
import com.github.mengxianun.core.json.Operator;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;

public class SQLBuilder {

	public static final String PREFIX_SELECT = "SELECT ";
	public static final String PREFIX_FROM = " FROM ";
	public static final String PREFIX_WHERE = " WHERE ";
	public static final String PREFIX_GROUP_BY = " GROUP BY ";
	public static final String PREFIX_HAVING = " HAVING ";
	public static final String PREFIX_ORDER_BY = " ORDER BY ";
	public static final String PREFIX_LIMIT = " LIMIT ";
	public static final String ORDER_ASC = " ASC";
	public static final String ORDER_DESC = " DESC";
	public static final String INNER_JOIN = " INNER JOIN ";
	public static final String LEFT_OUTER_JOIN = " LEFT JOIN ";
	public static final String RIGHT_OUTER_JOIN = " RIGHT JOIN ";
	public static final String FULL_OUTER_JOIN = " FULL JOIN ";
	public static final String JOIN_ON = " ON ";
	public static final String DELIM_COMMA = ", ";
	public static final String DELIM_AND = " AND ";
	public static final String DELIM_OR = " OR ";
	public static final String COUNT = " COUNT(*) ";
	public static final String COLUMN_ALL = "*";
	// 字段别名关联字符串
	public static final String ALIAS_KEY = " AS ";

	public static final String PREFIX_INSERT_INTO = "INSERT INTO ";
	public static final String PREFIX_UPDATE = "UPDATE ";
	public static final String UPDATE_SET = " SET ";
	public static final String PREFIX_DELETE_FROM = "DELETE FROM ";

	private Action action;
	private DataContext dataContext;
	private Dialect dialect;
	protected String sql;
	protected List<Object> params = new ArrayList<>();
	protected List<Object> whereParams = new ArrayList<>();
	// 关联分页查询的情况, SQL 语句构建做特殊处理
	protected boolean joinLimit;
	protected List<FilterItem> joinLimitFilterItems = new ArrayList<>();
	protected List<OrderItem> joinLimitOrderItems = new ArrayList<>();
	// 拼接后的字符串
	protected String columnString = "";
	protected String tableString = "";
	protected String joinString = "";
	protected String whereString = "";
	protected String groupString = "";
	protected String orderString = "";
	protected String limitString = "";
	protected String valueString = "";

	public SQLBuilder(Action action) {
		this.action = action;
		this.dataContext = action.getDataContext();
		this.dialect = dataContext.getDialect();
//		toSql();
	}

	public void toSql() {
		if (action.isDetail() || action.isSelect()) {
			toSelect();
		} else if (action.isInsert()) {
			toInsert();
		} else if (action.isUpdate()) {
			toUpdate();
		} else if (action.isDelete()) {
			toDelete();
		}
	}

	public void toSelect() {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(toColumns());
		sqlBuilder.append(toSelectTables());
		sqlBuilder.append(toJoins());
		sqlBuilder.append(toWhere());
		sqlBuilder.append(toGroups());
		sqlBuilder.append(toOrders());
		sqlBuilder.append(toLimit());
		sql = sqlBuilder.toString();
	}

	public void toInsert() {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(toInsertTable());
		sqlBuilder.append(toInsertValues());
		sql = sqlBuilder.toString();
	}

	public void toUpdate() {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(toUpdateTable());
		sqlBuilder.append(toUpdateValues());
		sqlBuilder.append(toWhere());
		sql = sqlBuilder.toString();
	}

	public void toDelete() {
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append(toDeleteTable());
		sqlBuilder.append(toWhere());
		sql = sqlBuilder.toString();
	}

	public String toColumns() {
		StringBuilder columnsBuilder = new StringBuilder(PREFIX_SELECT);
		List<ColumnItem> columnItems = action.getColumnItems();
		if (columnItems.isEmpty()) {
			columnsBuilder.append(COLUMN_ALL).append(" ");
		} else {
			boolean comma = false;
			for (ColumnItem columnItem : columnItems) {
				if (comma) {
					columnsBuilder.append(", ");
				}
				columnsBuilder.append(spliceColumn(columnItem));
				String alias = columnItem.getAlias();
				if (!Strings.isNullOrEmpty(alias) && dialect.columnAliasEnabled()) {
					columnsBuilder.append(ALIAS_KEY).append(alias);
				}
				comma = true;
			}
		}
		return columnString = columnsBuilder.toString();
	}

	public String toSelectTables() {
		StringBuilder tablesBuilder = new StringBuilder(PREFIX_FROM);
		List<TableItem> tableItems = action.getTableItems();
		boolean comma = false;
		for (TableItem tableItem : tableItems) {
			if (comma) {
				tablesBuilder.append(", ");
			}
			Table table = tableItem.getTable();
			if (table != null) {
				// join 和 limit 同时存在时, 并且存在一对多或多对多的情况下, 分页会出问题.
				// 这里将主表作为基础表(子查询), 特殊处理.
				if (!action.getJoinItems().isEmpty() && action.getLimitItem() != null) {
					// 基础表查询语句
					StringBuilder subBuilder = new StringBuilder(PREFIX_SELECT);
					subBuilder.append("*").append(PREFIX_FROM).append(table.getName());

					/*
					 * Where
					 */
					List<FilterItem> filterItems = action.getFilterItems();
					// 主表的 Where 条件
					List<FilterItem> mainTableFilterItems = filterItems.stream()
							.filter(e -> e.getColumnItem().getTableItem().getTable() == table)
							.collect(Collectors.toList());
					// 基础表查询属于子查询, 内部条件 SQL 不指定表别名
					String mainTableWhereString = toWhere(mainTableFilterItems, false);
					subBuilder.append(mainTableWhereString);

					joinLimitFilterItems = new ArrayList<>(action.getFilterItems());
					joinLimitFilterItems.removeAll(mainTableFilterItems);

					/*
					 * Order
					 */
					List<OrderItem> orderItems = action.getOrderItems();
					// 主表的 Order
					List<OrderItem> mainTableOrderItems = orderItems.stream()
							.filter(e -> e.getColumnItem().getTableItem().getTable() == table)
							.collect(Collectors.toList());
					String mainTableOrderString = toOrders(mainTableOrderItems, false);
					subBuilder.append(mainTableOrderString);

					joinLimitOrderItems = new ArrayList<>(action.getOrderItems());
					joinLimitOrderItems.removeAll(mainTableOrderItems);

					subBuilder.append(toLimit());
					// 拼接基础表查询语句
					tablesBuilder.append("(").append(subBuilder).append(")");
					joinLimit = true;
				} else {
					tablesBuilder.append(spliceTable(table));
				}
			} else {
				tablesBuilder.append(tableItem.getExpression());
			}
			String alias = tableItem.getAlias();
			if (!Strings.isNullOrEmpty(alias) && dialect.tableAliasEnabled()) {
				tablesBuilder.append(ALIAS_KEY).append(alias);
			}
			comma = true;
		}
		return tableString = tablesBuilder.toString();
	}

	public String toJoins() {
		List<JoinItem> joinItems = action.getJoinItems();
		if (joinItems.isEmpty()) {
			return "";
		}
		StringBuilder joinsBuilder = new StringBuilder();
		for (JoinItem joinItem : joinItems) {
			switch (joinItem.getJoinType()) {
			case LEFT:
				joinsBuilder.append(LEFT_OUTER_JOIN);
				break;
			case RIGHT:
				joinsBuilder.append(RIGHT_OUTER_JOIN);
				break;
			case INNER:
				joinsBuilder.append(INNER_JOIN);
				break;
			case FULL:
				joinsBuilder.append(FULL_OUTER_JOIN);
				break;

			default:
				throw new DataException(String.format("wrong join type [%s]", joinItem.getJoinType()));
			}

			List<ColumnItem> leftColumns = joinItem.getLeftColumns();
			List<ColumnItem> rightColumns = joinItem.getRightColumns();
			for (int i = 0; i < leftColumns.size(); i++) {
				// join left table
				ColumnItem leftColumnItem = leftColumns.get(i);
				TableItem leftTableItem = leftColumnItem.getTableItem();
				Table leftTable = leftTableItem.getTable();
				String leftTableAlias = leftTableItem.getAlias();
				// join right table
				ColumnItem rightColumnItem = rightColumns.get(i);
				TableItem rightTableItem = rightColumnItem.getTableItem();
				Table rightTable = rightTableItem.getTable();
				String rightTableAlias = rightTableItem.getAlias();
				if (i == 0) {
					joinsBuilder.append(quote(rightTable.getName()));
					if (!Strings.isNullOrEmpty(rightTableAlias) && dialect.tableAliasEnabled()) {
						joinsBuilder.append(ALIAS_KEY).append(rightTableAlias);
					}
					joinsBuilder.append(JOIN_ON);
				} else {
					joinsBuilder.append(DELIM_AND);
				}
				if (!Strings.isNullOrEmpty(leftTableAlias) && dialect.tableAliasEnabled()) {
					joinsBuilder.append(leftTableAlias);
				} else {
					joinsBuilder.append(quote(leftTable.getName()));
				}
				joinsBuilder.append(".").append(quote(leftColumnItem.getColumn().getName()));
				joinsBuilder.append(" = ");
				if (!Strings.isNullOrEmpty(rightTableAlias) && dialect.tableAliasEnabled()) {
					joinsBuilder.append(rightTableAlias);
				} else {
					joinsBuilder.append(quote(rightTable.getName()));
				}
				joinsBuilder.append(".").append(quote(rightColumnItem.getColumn().getName()));
			}

		}
		return joinString = joinsBuilder.toString();
	}

	public String toWhere() {
		return whereString = joinLimit ? toWhere(joinLimitFilterItems) : toWhere(action.getFilterItems());
	}

	public String toWhere(List<FilterItem> filterItems) {
		return toWhere(filterItems, null);
	}

	public String toWhere(List<FilterItem> filterItems, Boolean assignTableAlias) {
		if (filterItems.isEmpty()) {
			return "";
		}
		StringBuilder whereBuilder = new StringBuilder(PREFIX_WHERE);
		boolean first = true;
		for (FilterItem filterItem : filterItems) {
			String filterSql = toFilter(filterItem, assignTableAlias);
			if (first) {
				// 去掉开头的连接符
				filterSql = deleteFirstConnector(filterSql, filterItem.getConnector());
			}
			whereBuilder.append(filterSql);
			first = false;
		}
		return whereBuilder.toString();
	}

	public String toFilter(FilterItem filterItem) {
		return toFilter(filterItem, null);
	}

	public String toFilter(FilterItem filterItem, Boolean assignTableAlias) {
		StringBuilder filterBuilder = new StringBuilder();
		filterBuilder.append(" ").append(filterItem.getConnector()).append(" ");
		// 嵌套子条件
		List<FilterItem> subFilterItems = filterItem.getSubFilterItems();
		if (!subFilterItems.isEmpty()) {
			StringBuilder subFilterBuilder = new StringBuilder();
			subFilterItems.forEach(f -> subFilterBuilder.append(toFilter(f)));
			// 去掉开头的连接符
			String subFilterSql = deleteFirstConnector(subFilterBuilder.toString(),
					subFilterItems.get(0).getConnector());
			filterBuilder.append("(").append(subFilterSql).append(")");
			return filterBuilder.toString();
		}
		ColumnItem columnItem = filterItem.getColumnItem();
		filterBuilder.append(spliceColumn(columnItem, assignTableAlias));
		filterBuilder.append(" ");
		Object value = filterItem.getRealValue();
		Operator operator = filterItem.getOperator();
		switch (operator) {
		case EQUAL:
		case STRONG_EQUAL:
		case NOT_EQUAL:
		case LT:
		case LTE:
		case GT:
		case GTE:
		case LIKE:
		case NOT_LIKE:
			filterBuilder.append(operator.sql()).append(" ?");
			params.add(value);
			whereParams.add(value);
			break;
		case IN:
		case NOT_IN:
			Object[] inValue = (Object[]) value;
			filterBuilder.append(operator.sql()).append(" (?").append(Strings.repeat(",?", inValue.length - 1))
					.append(")");
			List<Object> inValueList = Arrays.asList(inValue);
			params.addAll(inValueList);
			whereParams.addAll(inValueList);
			break;
		case BETWEEN:
			filterBuilder.append("between ? and ?");
			List<Object> betweenValueList = Arrays.asList((Object[]) value);
			params.addAll(betweenValueList);
			whereParams.addAll(betweenValueList);
			break;

		default:
			break;
		}

		return filterBuilder.toString();
	}

	public String toGroups() {
		List<GroupItem> groupItems = action.getGroupItems();
		if (groupItems.isEmpty()) {
			return "";
		}
		StringBuilder groupsBuilder = new StringBuilder(PREFIX_GROUP_BY);
		boolean comma = false;
		for (GroupItem groupItem : groupItems) {
			if (comma) {
				groupsBuilder.append(", ");
			}
			ColumnItem columnItem = groupItem.getColumnItem();
			groupsBuilder.append(spliceColumn(columnItem));
			comma = true;
		}
		return groupString = groupsBuilder.toString();
	}

	public String toOrders() {
		return orderString = joinLimit ? toOrders(joinLimitOrderItems) : toOrders(action.getOrderItems());
	}

	public String toOrders(List<OrderItem> orderItems) {
		return toOrders(orderItems, null);
	}

	public String toOrders(List<OrderItem> orderItems, Boolean assignTableAlias) {
		if (orderItems.isEmpty()) {
			return "";
		}
		StringBuilder ordersBuilder = new StringBuilder(PREFIX_ORDER_BY);
		boolean comma = false;
		for (OrderItem orderItem : orderItems) {
			if (comma) {
				ordersBuilder.append(", ");
			}
			ColumnItem columnItem = orderItem.getColumnItem();
			ordersBuilder.append(spliceColumn(columnItem, assignTableAlias));

			switch (orderItem.getOrder()) {
			case DESC:
				ordersBuilder.append(ORDER_DESC);
				break;

			default:
				ordersBuilder.append(ORDER_ASC);
				break;
			}
			comma = true;
		}
		return orderString = ordersBuilder.toString();
	}

	public String toLimit() {
		if (joinLimit) {
			return "";
		}
		LimitItem limitItem = action.getLimitItem();
		if (limitItem == null) {
			return "";
		}
		params.add(limitItem.getLimit());
		params.add(limitItem.getStart());
		//		StringBuilder limitBuilder = new StringBuilder(PREFIX_LIMIT);
		//		limitBuilder.append("?, ?");
		//		return limitString = limitBuilder.toString();
		return limitString = " LIMIT ? OFFSET ?";
	}

	public String toInsertTable() {
		List<TableItem> tableItems = action.getTableItems();
		StringBuilder tableBuilder = new StringBuilder(PREFIX_INSERT_INTO);
		Table table = tableItems.get(0).getTable();
		tableBuilder.append(spliceTable(table));
		return tableString = tableBuilder.toString();

	}

	public String toInsertValues() {
		List<ValueItem> valueItems = action.getValueItems();
		StringBuilder valuesBuilder = new StringBuilder();
		StringBuilder tempColumnsBuilder = new StringBuilder("(");
		StringBuilder tempValuesBuilder = new StringBuilder(" VALUES(");
		boolean comma = false;
		for (ValueItem valueItem : valueItems) {
			if (comma) {
				tempColumnsBuilder.append(", ");
				tempValuesBuilder.append(", ");
			}
			Column column = valueItem.getColumn();
			Object value = valueItem.getRealValue();
			tempColumnsBuilder.append(quote(column.getName()));
			if (column.getType().isJson()) {
				tempValuesBuilder.append(dialect.getJsonPlaceholder());
			} else {
				tempValuesBuilder.append("?");
			}
			params.add(value);
			comma = true;
		}
		tempColumnsBuilder.append(")");
		tempValuesBuilder.append(")");
		valuesBuilder.append(tempColumnsBuilder).append(tempValuesBuilder);
		return valueString = valuesBuilder.toString();
	}

	public String toUpdateTable() {
		List<TableItem> tableItems = action.getTableItems();
		StringBuilder tableBuilder = new StringBuilder(PREFIX_UPDATE);
		tableBuilder.append(spliceTable(tableItems.get(0)));
		return tableString = tableBuilder.toString();
	}

	public String toUpdateValues() {
		List<ValueItem> valueItems = action.getValueItems();
		StringBuilder valuesBuilder = new StringBuilder(UPDATE_SET);
		boolean comma = false;
		for (ValueItem valueItem : valueItems) {
			if (comma) {
				valuesBuilder.append(", ");
			}
			Column column = valueItem.getColumn();
			Object value = valueItem.getRealValue();
			valuesBuilder.append(quote(column.getName())).append(" = ");
			if (column.getType().isJson()) {
				valuesBuilder.append(dialect.getJsonPlaceholder());
			} else {
				valuesBuilder.append("?");
			}
			params.add(value);
			comma = true;
		}
		return valueString = valuesBuilder.toString();
	}

	public String toDeleteTable() {
		List<TableItem> tableItems = action.getTableItems();
		StringBuilder tableBuilder = new StringBuilder(PREFIX_DELETE_FROM);
		tableBuilder.append(spliceTable(tableItems.get(0)));
		return tableString = tableBuilder.toString();
	}

	public String deleteFirstConnector(String sql, Connector connector) {
		switch (connector) {
		case AND:
			return sql.replaceFirst(DELIM_AND, "");
		case OR:
			return sql.replaceFirst(DELIM_OR, "");

		default:
			return sql;
		}
	}

	public String spliceTable(TableItem tableItem) {
		StringBuilder tableBuilder = new StringBuilder();
		Table table = tableItem.getTable();
		tableBuilder.append(spliceTable(table));
		String alias = tableItem.getAlias();
		if (!Strings.isNullOrEmpty(alias) && dialect.tableAliasEnabled()) {
			tableBuilder.append(ALIAS_KEY).append(alias);
		}
		return tableBuilder.toString();
	}

	public String spliceTable(Table table) {
		StringBuilder tableBuilder = new StringBuilder();
		if (dialect.assignDatabase()) {
			Schema schema = table.getSchema();
			tableBuilder.append(quote(schema.getName())).append(".");
		}
		tableBuilder.append(quote(table.getName()));
		return tableBuilder.toString();
	}

	public String spliceColumn(ColumnItem columnItem) {
		return spliceColumn(columnItem, null);
	}

	/**
	 * 拼接列 在启动了表别名的情况下, 如果所属表指定了别名, 以表别名作为前缀, 否则以表名作为前缀. 如果没有启动表别名, 不添加前缀
	 * 
	 * @param columnItem
	 * @return
	 */
	public String spliceColumn(ColumnItem columnItem, Boolean assignTableAlias) {
		StringBuilder columnBuilder = new StringBuilder();
		Column column = columnItem.getColumn();
		if (column == null) {
			columnBuilder.append(columnItem.getExpression());
		} else {
			if (dialect.tableAliasEnabled() && (assignTableAlias == null || assignTableAlias)) {
				TableItem tableItem = columnItem.getTableItem();
				String tableAlias = tableItem.getAlias();
				if (!Strings.isNullOrEmpty(tableAlias)) {
					columnBuilder.append(tableAlias);
				} else {
					Table table = tableItem.getTable();
					if (table != null) {
						columnBuilder.append(table.getName());
					}
				}
				columnBuilder.append(".");
			}
			columnBuilder.append(quote(column.getName()));
		}
		return columnBuilder.toString();
	}

	/**
	 * <span>拼接 GROUP/WHERE/ORDER 列</span>
	 * 在启动了表别名的而情况下, 如果所属表指定了别名, 以表别名作为前缀, 否则以表名作为前缀. 如果没有启动表别名, 不添加前缀
	 * 
	 * @param columnItem
	 * @return
	 */
	public String spliceCondColumn(ColumnItem columnItem) {
		StringBuilder columnBuilder = new StringBuilder();
		Column column = columnItem.getColumn();
		if (column == null) {
			columnBuilder.append(columnItem.getExpression());
		} else {
			if (dialect.tableAliasEnabled()) {
				TableItem tableItem = columnItem.getTableItem();
				String tableAlias = tableItem.getAlias();
				if (!Strings.isNullOrEmpty(tableAlias)) {
					columnBuilder.append(tableAlias);
				} else {
					Table table = tableItem.getTable();
					if (table != null) {
						columnBuilder.append(table.getName());
					}
				}
				columnBuilder.append(".");
			}
			if (dialect.columnAliasEnabled() && !Strings.isNullOrEmpty(columnItem.getAlias())) {
				columnBuilder.append(columnItem.getAlias());
			} else {
				columnBuilder.append(quote(column.getName()));
			}
		}
		return columnBuilder.toString();
	}

	/**
	 * 用引用符号包裹数据元素
	 * 
	 * @param element
	 * @return
	 */
	public String quote(String element) {
		if (dialect.quoteTable() || SqlReservedWords.containsWord(element)) {
			String identifierQuoteString = dataContext.getIdentifierQuoteString();
			return identifierQuoteString + element + identifierQuoteString;
		}
		return element;
	}

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

		String mainColumnString = columnsBuilder.toString();
		StringBuilder originalBuilder = new StringBuilder();
		// 原始SQL
		// 1. 只查询主表的列
		// 2. 去掉 LIMIT 条件
		String originalSql = originalBuilder.append(PREFIX_SELECT).append("distinct ").append(mainColumnString)
				.append(" ")
				.append(tableString).append(joinString)
				.append(whereString).append(groupString).toString();
		originalSql = originalSql.replace(Strings.nullToEmpty(limitString), "");
		StringBuilder countBuilder = new StringBuilder();
		StringBuilder countSql = countBuilder.append(PREFIX_SELECT).append(COUNT).append(ALIAS_KEY).append("count")
				.append(PREFIX_FROM)
				.append("(")
				.append(originalSql)
				.append(")").append(ALIAS_KEY).append("original_table");
		return countSql.toString();
	}

	public List<Object> countParams() {
		return whereParams;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public List<Object> getParams() {
		return params;
	}

	public void setParams(List<Object> params) {
		this.params = params;
	}

}

package com.github.mengxianun.core.parser.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.Nullable;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.item.GroupItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.JoinTableItem;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.OrderItem;
import com.github.mengxianun.core.item.RelationshipItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.item.ValueItem;
import com.github.mengxianun.core.item.extension.StatementFilterItem;
import com.github.mengxianun.core.item.extension.StatementValueFilterItem;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.parser.info.ConditionInfo;
import com.github.mengxianun.core.parser.info.FileInfo;
import com.github.mengxianun.core.parser.info.FilterInfo;
import com.github.mengxianun.core.parser.info.GroupInfo;
import com.github.mengxianun.core.parser.info.JoinInfo;
import com.github.mengxianun.core.parser.info.LimitInfo;
import com.github.mengxianun.core.parser.info.OrderInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.parser.info.ValuesInfo;
import com.github.mengxianun.core.parser.info.WhereInfo;
import com.github.mengxianun.core.parser.info.extension.StatementConditionInfo;
import com.github.mengxianun.core.parser.info.extension.StatementValueConditionInfo;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.JoinType;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.Order;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.TableSettings;
import com.github.mengxianun.core.schema.relationship.Relationship;
import com.github.mengxianun.core.schema.relationship.RelationshipPath;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table.Cell;

public class CRUDActionParser extends AbstractActionParser {

	private final Action action;
	// 主表的TableItems
	private Map<Table, TableItem> tempTableItems = new LinkedHashMap<>();
	// Join表的TableItems
	private Map<Table, TableItem> tempJoinTableItems = new LinkedHashMap<>();
	// 关联表的TableItems, row 为 Table, column 为 表别名
	private HashBasedTable<Table, String, TableItem> tempRelationTableItems = HashBasedTable.create();
	// Join table join type
	private Map<Table, JoinType> tempJoinTypes = new HashMap<>();
	// key: column alias, value: ColumnItem
	private Map<String, ColumnItem> tempAliasColumnItems = new HashMap<>();
	private Map<Column, ColumnItem> tempColumnItems = new HashMap<>();

	// 已经存在关联关系路径, 用户后续循环复用, 避免关联表多次Join
	// 注: 这里是路径复用, 不是关系复用
	// Key 为关系路径, Value 为关系路径Item
	Map<RelationshipPath, List<RelationshipItem>> existRelationshipItems = new HashMap<>();

	public CRUDActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
		this.action = new Action(dataContext);
	}

	@Override
	public NewAction parse() {
		Operation operation = simpleInfo.operation();
		if (operation == Operation.SELECT_DISTINCT) {
			operation = Operation.SELECT;
			action.setDistinct(true);
		}
		action.setOperation(operation);

		parseTables();

		switch (operation) {
		case DETAIL:
		case QUERY:
		case SELECT:
			parseSelect();
			break;
		case INSERT:
			parseInsert();
			break;
		case UPDATE:
			parseUpdate();
			break;
		case DELETE:
			parseDelete();
			break;

		default:
			break;
		}

		parseFile();
		finish();

		return action;
	}

	public void parseSelect() {
		parseJoins();
		parseColumns();
		parseWhere();
		parseGroups();
		parseOrders();
		parseLimit();
	}

	public void parseInsert() {
		parseInsertValues();
	}

	public void parseUpdate() {
		parseUpdateValues();
		parseWhere();
	}

	public void parseDelete() {
		parseWhere();
	}

	/**
	 * parse primary table node
	 */
	public void parseTables() {
		parseTables(simpleInfo.table());
	}

	/**
	 * Parse primary table node.
	 * 
	 * @param tableInfo
	 */
	public void parseTables(TableInfo tableInfo) {
		TableItem tableItem = createTableItem(tableInfo);
		Table table = tableItem.getTable();

		tempTableItems.put(table, tableItem);
		action.addTableItem(tableItem);
		action.addTable(table);
	}

	private TableItem createTableItem(TableInfo tableInfo) {
		return createTableItem(tableInfo.table(), tableInfo.alias());
	}

	private TableItem createTableItem(String tableName, String alias) {
		TableItem tableItem;
		Table table = dataContext.getTable(tableName);
		boolean customAlias = false;
		if (Strings.isNullOrEmpty(alias) && action.isQuery()) { // Select specify alias, other operations do not specify alias
			alias = getAlias(table);
		} else {
			customAlias = true;
		}
		if (table == null) {
			if (dataContext.getDialect().validTableExists()) {
				throw new DataException(ResultStatus.DATASOURCE_TABLE_NOT_EXIST, tableName);
			}
			tableItem = new TableItem(tableName, alias, customAlias);
		} else {
			tableItem = new TableItem(table, alias, customAlias);
		}

		return tableItem;
	}

	public void parseJoins() {
		List<JoinInfo> joins = simpleInfo.joins();
		List<JoinElement> joinElements = joins.stream().map(e -> parseJoin(e.joinType(), e.tableInfo()))
				.collect(Collectors.toList());
		buildJoin(joinElements);
	}

	public JoinElement parseJoin(JoinType joinType, TableInfo tableInfo) {
		TableItem joinTableItem = createTableItem(tableInfo);
		Table joinTable = joinTableItem.getTable();
		// Record the join table join type
		tempJoinTypes.put(joinTable, joinType);

		tempJoinTableItems.put(joinTable, joinTableItem);
		action.addJoinTable(joinTable);
		return new JoinElement(joinTableItem, joinType);
	}

	/**
	 * Build Join
	 * 
	 * @param joinElements
	 */
	public void buildJoin(List<JoinElement> joinElements) {
		// 获取所有关系路径
		Set<RelationshipPath> relationshipPaths = getRelationshipPaths(joinElements);

		for (RelationshipPath relationshipPath : relationshipPaths) {
			createRelationship(relationshipPath);
		}
	}

	/**
	 * 以主表开始, 以join表请求的顺序为准, 查找请求的所有表的关联关系路径.
	 * 
	 * @param joinElements
	 * @return
	 */
	private Set<RelationshipPath> getRelationshipPaths(List<JoinElement> joinElements) {
		Table table = action.getPrimaryTable();
		// Calculate the order of request tables
		AtomicInteger index = new AtomicInteger();
		Map<Table, Integer> tableOrder = new HashMap<>();
		joinElements.forEach(e -> tableOrder.put(e.getJoinTableItem().getTable(), index.getAndIncrement()));

		// Find relationship
		Set<RelationshipPath> relationshipPaths = new LinkedHashSet<>();
		for (JoinElement joinElement : joinElements) {
			Table joinTable = joinElement.getJoinTableItem().getTable();
			Set<RelationshipPath> tempRelationshipPaths = dataContext.getRelationships(table, joinTable);
			if (tempRelationshipPaths.isEmpty()) {
				throw new DataException(String.format("Association relation not found for the table [%s] and [%s]",
						table.getName(), joinTable.getName()));
			}
			// 关联关系获取逻辑
			// 1. 顺序一致, 如请求的join表为[B, C], 则关联关系只能是B-C, 不能是C-B, 否则会造成多层join
			// 2. 在多个关联关系路径中, 优先请求表的关系路径, 其次非请求表关系的最短路径
			// 如: 已知的关系为, A-B, A-C, A-B-C, A-H-C
			// 例, 请求 A, join [B,C], 获取的关系为: A-B, A-C, A-B-C
			// 例, 请求A, join [C], 获取的关系为: A-C. 这里没有A-B-C, 因为B表不再请求中. 也没有A-H-C, 因为A-C和A-H-C中最短的路径为A-C
			Set<RelationshipPath> requestRelationshipPaths = new LinkedHashSet<>();
			for (RelationshipPath relationshipPath : tempRelationshipPaths) {
				// Order is the same as the request order of the join tables
				boolean order = true;
				// The first table is the main table
				boolean first = true;
				// If the tables on the relational path are all tables in the request
				boolean isRequestRelationshipPath = true;
				// The previous table in the request
				Table preTable = table;
				for (Relationship relationship : relationshipPath.getRelationships()) {
					Table primaryTable = relationship.getPrimaryColumn().getTable();
					Table foreignTable = relationship.getForeignColumn().getTable();

					if (first) { // Skip main table
						first = false;
						continue;
					}
					if (action.isJoinTable(primaryTable)) {
						preTable = primaryTable;
					}
					if (action.isJoinTable(foreignTable)) {
						Integer preIndex = tableOrder.containsKey(preTable) ? tableOrder.get(preTable) : -1;
						Integer nextIndex = tableOrder.get(foreignTable);
						if (preIndex > nextIndex) {
							order = false;
							break;
						}
					}
					if (!action.isJoinTable(primaryTable) || !action.isJoinTable(foreignTable)) {
						isRequestRelationshipPath = false;
					}

				}
				if (order && isRequestRelationshipPath) {
					requestRelationshipPaths.add(relationshipPath);
				}
			}
			Set<RelationshipPath> parsedRelationshipPaths = requestRelationshipPaths;
			if (parsedRelationshipPaths.isEmpty()) {
				// Shortest relation path length
				Optional<Integer> minLenOptional = tempRelationshipPaths.parallelStream().map(RelationshipPath::size)
						.min(Comparator.comparing(Integer::valueOf));
				if (minLenOptional.isPresent()) {
					Integer minLen = minLenOptional.get();
					parsedRelationshipPaths = tempRelationshipPaths.stream().filter(e -> e.size() == minLen)
							.collect(Collectors.toSet());
				}
			}
			relationshipPaths.addAll(parsedRelationshipPaths);
		}
		return relationshipPaths;
	}

	private void createRelationship(RelationshipPath relationshipPath) {
		// 上级 TableItem, 记录此值, 保证在多表多字段关联时, join操作正确的表
		// 例: 当前循环的关联路径为 A-B-C, 当循环到A-B的时候, 上级TableItem为A, 当循环到B-C的时候, 上级TableItem为B
		Table firstTable = relationshipPath.getFirst().getPrimaryColumn().getTable();
		TableItem preTableItem = getTableItem(firstTable);

		// 当前循环中的关联关系
		Set<Relationship> currentRelationships = new LinkedHashSet<>();
		List<RelationshipItem> currentRelationshipItems = new ArrayList<>();

		for (Relationship relationship : relationshipPath.getRelationships()) {
			currentRelationships.add(relationship);

			// 当前循环的关联关系, 不可改变
			Set<Relationship> currentFixedRelationships = new LinkedHashSet<>(currentRelationships);
			List<RelationshipItem> currentFixedRelationshipItems = new ArrayList<>(currentRelationshipItems);

			RelationshipPath existRelationshipPath = new RelationshipPath(currentFixedRelationships);
			if (existRelationshipItems.containsKey(existRelationshipPath)) {
				currentRelationshipItems = existRelationshipItems.get(existRelationshipPath);
			} else {
				if (!currentRelationshipItems.isEmpty()) {
					// 在已存在的关联关系中的取最后一个, 为当前循环的左表TableItem
					preTableItem = currentRelationshipItems.get(currentRelationshipItems.size() - 1)
							.getRightTableItem();
				}
				Column primaryColumn = relationship.getPrimaryColumn();
				Column foreignColumn = relationship.getForeignColumn();
				Table foreignTable = foreignColumn.getTable();

				JoinTableItem foreignTableItem = new JoinTableItem(foreignTable, getAlias(foreignTable), false,
						currentFixedRelationshipItems);
				tempRelationTableItems.put(foreignTable, foreignTableItem.getAlias(), foreignTableItem);
				// update JoinTableItems
				if (tempJoinTableItems.containsKey(foreignTable)) {
					tempJoinTableItems.put(foreignTable, foreignTableItem);
				}

				ColumnItem primaryColumnItem = new ColumnItem(primaryColumn, preTableItem);
				ColumnItem foreignColumnItem = new ColumnItem(foreignColumn, foreignTableItem);
				JoinType joinType = tempJoinTypes.containsKey(foreignColumn.getTable())
						? tempJoinTypes.get(foreignColumn.getTable())
						: JoinType.LEFT;

				action.addJoinItem(new JoinItem(primaryColumnItem, foreignColumnItem, joinType));
				// 添加关联关系最后一个Item
				RelationshipItem relationshipItem = new RelationshipItem(preTableItem, foreignTableItem, relationship);
				currentFixedRelationshipItems.add(relationshipItem);
				// 记录添加的关联关系Item
				existRelationshipItems.put(existRelationshipPath, currentFixedRelationshipItems);

				currentRelationshipItems = new ArrayList<>(currentRelationshipItems);
				currentRelationshipItems.add(relationshipItem);
			}
		}
	}

	class JoinElement {

		TableItem joinTableItem;
		JoinType joinType;

		public JoinElement(TableItem joinTableItem, JoinType joinType) {
			this.joinTableItem = joinTableItem;
			this.joinType = joinType;
		}

		public TableItem getJoinTableItem() {
			return joinTableItem;
		}

		public JoinType getJoinType() {
			return joinType;
		}

	}

	public void parseColumns() {
		List<ColumnItem> columnItems = new ArrayList<>();
		List<ColumnInfo> columns = simpleInfo.columns();
		// Query all fields when no specific fields are specified
		if (columns.isEmpty() && action.isQuery()) {
			columnItems.addAll(createAllColumns());
		} else {
			for (ColumnInfo columnInfo : columns) {
				columnItems.addAll(parseColumn(columnInfo));
			}
		}
		removeExcludeColumns(columnItems);
		removeMaxColumns(columnItems);
		columnItems.forEach(e -> {
			action.addColumnItem(e);
			tempAliasColumnItems.put(e.getAlias(), e);
			if (e.getColumn() != null) {
				tempColumnItems.put(e.getColumn(), e);
			}
		});
	}

	/**
	 * Parse fields node
	 * 
	 * @param columnInfo
	 * @return
	 */
	private List<ColumnItem> parseColumn(ColumnInfo columnInfo) {
		String tableName = columnInfo.table();
		String columnName = columnInfo.column();
		if ("*".equals(columnName)) { // All columns, "fields": "*"
			if (Strings.isNullOrEmpty(tableName)) {
				return createAllColumns();
			} else { // Specifies table's all columns
				return createTableColumns(tableName);
			}
		}
		ColumnItem columnItem = createColumnItem(columnInfo);
		return Lists.newArrayList(columnItem);
	}

	/**
	 * Add all the columns of all the tables (the primary table and the join table)
	 */
	private List<ColumnItem> createAllColumns() {
		List<ColumnItem> columnItems = new ArrayList<>();
		columnItems.addAll(createMainColumns());
		columnItems.addAll(createJoinColumns());
		return columnItems;
	}

	/**
	 * Add all columns of the primary table
	 */
	private List<ColumnItem> createMainColumns() {
		List<ColumnItem> columnItems = new ArrayList<>();
		tempTableItems.values().stream().forEach(e -> columnItems.addAll(createMainTableItemColumns(e)));
		return columnItems;
	}

	/**
	 * Create all the columns of the join table
	 */
	private List<ColumnItem> createJoinColumns() {
		List<ColumnItem> columnItems = new ArrayList<>();
		Set<Cell<Table, String, TableItem>> cellSet = tempRelationTableItems.cellSet();
		for (Cell<Table, String, TableItem> cell : cellSet) {
			@Nullable
			Table table = cell.getRowKey();
			@Nullable
			TableItem tableItem = cell.getValue();
			if (tempJoinTableItems.containsKey(table)) {
				List<ColumnItem> tableItemColumns = createJoinTableItemColumns(tableItem);
				columnItems.addAll(tableItemColumns);
			}
		}
		return columnItems;
	}

	private void removeExcludeColumns(List<ColumnItem> columnItems) {
		List<ColumnInfo> excludeColumns = simpleInfo.excludeColumns();
		List<ColumnItem> removeColumnItems = new ArrayList<>();
		for (ColumnInfo columnInfo : excludeColumns) {
			for (ColumnItem columnItem : columnItems) {
				Column column = columnItem.getColumn();
				if (column != null) {
					String columnName = column.getName();
					String tableName = column.getTable().getName();
					if (columnName.equalsIgnoreCase(columnInfo.column())
							&& tableName.equalsIgnoreCase(columnInfo.table())) {
						removeColumnItems.add(columnItem);
					}
				}
			}
		}
		columnItems.removeAll(removeColumnItems);
	}

	/**
	 * If the query column exceeds the maximum query column setting for the table,
	 * delete the columns that exceed
	 * 
	 * @param columnItems
	 */
	private void removeMaxColumns(List<ColumnItem> columnItems) {
		Table primaryTable = action.getPrimaryTable();
		TableSettings settings = action.getPrimaryTable().getSettings();
		int maxQueryFields = settings.maxQueryFields();

		List<ColumnItem> removeColumnItems = new ArrayList<>();
		int primaryColumnNum = 0;
		for (ColumnItem columnItem : columnItems) {
			Column column = columnItem.getColumn();
			// Only process the primary table for now
			if (column != null && column.getTable() == primaryTable) {
				primaryColumnNum++;
				if (primaryColumnNum > maxQueryFields) {
					removeColumnItems.add(columnItem);
				}
			}
		}
		columnItems.removeAll(removeColumnItems);
	}

	/**
	 * Create all columns item of the specified table
	 * 
	 * @param tableName
	 */
	private List<ColumnItem> createTableColumns(String tableName) {
		if (Strings.isNullOrEmpty(tableName)) {
			return Collections.emptyList();
		}
		Table table = dataContext.getTable(tableName);
		if (tempTableItems.containsKey(table)) {
			return createMainTableItemColumns(tempTableItems.get(table));
		} else if (tempJoinTableItems.containsKey(table)) {
			return createJoinTableItemColumns(tempJoinTableItems.get(table));
		}
		return Collections.emptyList();
	}

	private List<ColumnItem> createMainTableItemColumns(TableItem tableItem) {
		Table table = tableItem.getTable();
		if (table == null) {
			return Collections.emptyList();
		}
		return table.getColumns().stream()
				.map(e -> new ColumnItem(e, getAlias(e), false, tableItem)).collect(Collectors.toList());
	}

	private List<ColumnItem> createJoinTableItemColumns(TableItem tableItem) {
		Table table = tableItem.getTable();
		if (table == null) {
			return Lists.newArrayList(new ColumnItem("*"));
		}
		return table.getColumns().stream()
				.map(e -> new JoinColumnItem(e, getAlias(e), false, tableItem)).collect(Collectors.toList());
	}

	/**
	 * Parse where
	 */
	public void parseWhere() {
		WhereInfo whereInfo = simpleInfo.where();
		if (whereInfo != null) {
			whereInfo.filters().forEach(e -> action.addFilterItem(parseFilter(e)));
		}
		parseStatementFilters();
	}

	/**
	 * Parse every filter element
	 * 
	 * @param filterInfo
	 * @return
	 */
	private FilterItem parseFilter(FilterInfo filterInfo) {
		Connector connector = filterInfo.connector();
		ConditionInfo conditionInfo = filterInfo.conditionInfo();
		List<FilterInfo> subfilters = filterInfo.subfilters();
		if (!subfilters.isEmpty()) {
			List<FilterItem> subFilterItems = subfilters.stream().map(this::parseFilter).collect(Collectors.toList());
			return new FilterItem(connector, subFilterItems);
		} else {
			return parseFilter(connector, conditionInfo);
		}
	}

	/**
	 * Parse filter condition
	 * 
	 * @param conditionInfo
	 * @return
	 */
	private FilterItem parseFilter(Connector connector, ConditionInfo conditionInfo) {
		Operator operator = conditionInfo.operator();
		ColumnInfo columnInfo = conditionInfo.columnInfo();
		Object value = conditionInfo.value();

		ColumnItem columnItem = findColumnItem(columnInfo);
		// The column was not found in the requested tables (primary tables and join talbes)
		if (columnItem == null) {
			columnItem = createScatteredColumnItem(columnInfo);
		}
		return new FilterItem(connector, columnItem, operator, value);
	}

	public void parseStatementFilters() {
		List<StatementConditionInfo> statementConditions = simpleInfo.statementConditions();
		for (StatementConditionInfo statementConditionInfo : statementConditions) {
			Connector connector = statementConditionInfo.connector();
			String statement = statementConditionInfo.statement();
			StatementFilterItem statementFilterItem = new StatementFilterItem(connector, statement);
			action.addFilterItem(statementFilterItem);
		}
		List<StatementValueConditionInfo> statementValueConditionInfos = simpleInfo.statementValueConditions();
		for (StatementValueConditionInfo statementValueConditionInfo : statementValueConditionInfos) {
			Connector connector = statementValueConditionInfo.connector();
			Operator operator = statementValueConditionInfo.operator();
			ColumnInfo columnInfo = statementValueConditionInfo.columnInfo();
			String statement = statementValueConditionInfo.statement();

			ColumnItem columnItem = findColumnItem(columnInfo);
			// The column was not found in the requested tables (primary tables and join talbes)
			if (columnItem == null) {
				columnItem = createScatteredColumnItem(columnInfo);
			}
			StatementValueFilterItem statementValueFilterItem = new StatementValueFilterItem(connector, columnItem,
					operator,
					statement);
			action.addFilterItem(statementValueFilterItem);
		}
	}

	public void parseGroups() {
		List<GroupInfo> groups = simpleInfo.groups();
		groups.forEach(e -> action.addGroupItem(parseGroup(e)));
	}

	public GroupItem parseGroup(GroupInfo groupInfo) {
		ColumnItem columnItem = findColumnItem(groupInfo.columnInfo());
		return new GroupItem(columnItem);
	}

	public void parseOrders() {
		List<OrderInfo> orders = simpleInfo.orders();
		orders.forEach(e -> action.addOrderItem(parseOrder(e)));
	}

	public OrderItem parseOrder(OrderInfo orderInfo) {
		Order order = orderInfo.order();
		ColumnInfo columnInfo = orderInfo.columnInfo();
		ColumnItem columnItem = findColumnItem(columnInfo);
		return new OrderItem(columnItem, order);
	}

	public void parseLimit() {
		LimitInfo limitInfo = simpleInfo.limit();
		if (limitInfo != null) {
			action.addLimitItem(new LimitItem(limitInfo.start(), limitInfo.end()));
		}
	}

	public void parseInsertValues() {
		List<ValuesInfo> insertValues = simpleInfo.insertValues();
		List<List<ValueItem>> insertValueItems = insertValues.stream().map(this::parseValues)
				.collect(Collectors.toList());
		action.addAllInsertValueItems(insertValueItems);
	}

	public void parseUpdateValues() {
		List<ValueItem> valueItems = parseValues(simpleInfo.updateValues());
		action.addAllUpdateValueItem(valueItems);

	}

	private List<ValueItem> parseValues(ValuesInfo valuesInfo) {
		List<ValueItem> valueItems = new ArrayList<>();
		Map<String, Object> values = valuesInfo.values();
		for (Entry<String, Object> entry : values.entrySet()) {
			String columnName = entry.getKey();
			Object value = entry.getValue();
			Column column = dataContext.getColumn(simpleInfo.table().table(), columnName);
			if (column != null) { // Ignore incorrect column
				valueItems.add(new ValueItem(column, value));
			}
		}
		return valueItems;
	}

	private ColumnItem createColumnItem(ColumnInfo columnInfo) {
		String alias = columnInfo.alias();
		boolean customAlias = false;
		if (Strings.isNullOrEmpty(alias)) {
			alias = getAlias(null);
		} else {
			customAlias = true;
		}
		Column column = findColumn(columnInfo);
		if (column == null) { // May be: function...
			return new ColumnItem(columnInfo.column(), alias, customAlias);
		} else {
			TableItem tableItem = getMainTableItem(column.getTable());
			if (tableItem != null) {
				return new ColumnItem(column, alias, customAlias, tableItem);
			} else {
				tableItem = getJoinTableItem(column.getTable());
				if (tableItem != null) {
					return new JoinColumnItem(column, alias, customAlias, tableItem);
				}
			}
		}
		return null;
	}

	/**
	 * The column was not found in the requested tables (primary tables and join
	 * talbes)
	 * 
	 * @param columnInfo
	 * @return
	 */
	private JoinColumnItem createScatteredColumnItem(ColumnInfo columnInfo) {
		Table table = dataContext.getTable(columnInfo.table());
		if (tempRelationTableItems.containsRow(table)) {
			TableItem tableItem = tempRelationTableItems.values().iterator().next();
			return new JoinColumnItem(findColumn(columnInfo), columnInfo.alias(), false, tableItem);
		} else {
			// 1. add join table
			JoinElement joinElement = parseJoin(JoinType.LEFT,
					TableInfo.create(columnInfo.source(), columnInfo.table(), null));
			buildJoin(Lists.newArrayList(joinElement));
			// 2. create join column item
			TableItem tableItem = tempRelationTableItems.row(table).values().iterator().next();
			return new JoinColumnItem(findColumn(columnInfo), columnInfo.alias(), false, tableItem);
		}
	}

	private Column findColumn(ColumnInfo columnInfo) {
		String table = columnInfo.table();
		String column = columnInfo.column();
		if (Strings.isNullOrEmpty(table)) { // Default primary table column
			table = simpleInfo.table().table();
		}
		return dataContext.getColumn(table, column);
	}

	/**
	 * 表存在于请求中指定的查询表和关联表, 不存在的表只有一种可能, 就是在 查询关联的时候, 找到了请求中没有指定关联的表, 这个表一定是关联表.
	 * 
	 * @param table
	 * @return
	 */
	private TableItem getTableItem(Table table) {
		TableItem tableItem = null;
		if (tempTableItems.containsKey(table)) {
			tableItem = tempTableItems.get(table);
		} else if (tempRelationTableItems.containsRow(table)) {
			tableItem = tempRelationTableItems.row(table).values().iterator().next();
		} else if (tempJoinTableItems.containsKey(table)) {
			tableItem = tempJoinTableItems.get(table);
		} else {
			tableItem = new TableItem(table, getAlias(table), false);
			tempRelationTableItems.put(table, tableItem.getAlias(), tableItem);
		}
		return tableItem;
	}

	private TableItem getMainTableItem(Table table) {
		for (TableItem tableItem : action.getTableItems()) {
			Table mainTable = tableItem.getTable();
			if (mainTable == table) {
				return tableItem;
			}
		}
		return null;
	}

	private TableItem getJoinTableItem(Table table) {
		for (JoinItem joinItem : action.getJoinItems()) {
			TableItem tableItem = joinItem.getRightColumn().getTableItem();
			Table joinTable = tableItem.getTable();
			if (joinTable == table) {
				return tableItem;
			}
		}
		return null;
	}

	/**
	 * Look for ColumnItem in the column that has been parsed by the column
	 * alias or column name, If not, create a new ColumnItem
	 * 
	 * @param columnInfo
	 * @return
	 */
	private ColumnItem findColumnItem(ColumnInfo columnInfo) {
		String columnName = columnInfo.column();
		// priority alias
		if (tempAliasColumnItems.containsKey(columnName)) {
			return tempAliasColumnItems.get(columnName);
		}

		Column column = findColumn(columnInfo);
		if (column != null && tempColumnItems.containsKey(column)) {
			return tempColumnItems.get(column);
		}
		return createColumnItem(columnInfo);
	}

	private void parseFile() {
		FileInfo fileInfo = simpleInfo.file();
		if (fileInfo != null) {
			action.setFile(fileInfo.file());
		}
	}

	private void finish() {}

}

package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.JsonDataException;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.item.GroupItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.OrderItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.item.ValueItem;
import com.github.mengxianun.core.request.AdditionalKeywords;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.JoinType;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.Order;
import com.github.mengxianun.core.request.RequestKeyword;
import com.github.mengxianun.core.request.Template;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Json 解析器
 * 
 * @author mengxiangyun
 *
 */
public class JsonParser {

	// 表名或别名规则
	private final String wordRegex = "^(?![0-9]*$)[a-zA-Z0-9_$]+$";

	// Json 对象
	private final JsonObject jsonData;
	// 操作类型
	private Operation operation;
	private String operationAttribute;
	// 解析结果对象
	private Action action = new Action();
	// 表关联关系, 内部 List Table 从左到右依次关联. 不包含主表
	private List<List<Table>> tempJoins = new ArrayList<>();
	// 主表的TableItems
	private Map<Table, TableItem> tempTableItems = new LinkedHashMap<>();
	// Join表的TableItems
	private Map<Table, TableItem> tempJoinTableItems = new LinkedHashMap<>();
	// 关联表的TableItems(未出现在请求中的关联表)
	// 查询为 Select A, Join C, 实际关系为A-B-C, 所以这里存储的是B
	private Map<Table, TableItem> tempRelationTableItems = new LinkedHashMap<>();
	// 保留
	//	private Map<Column, ColumnItem> tempColumnItems = new LinkedHashMap<>();

	public JsonParser(String json) {
		this(new com.google.gson.JsonParser().parse(json).getAsJsonObject());
	}

	public JsonParser(JsonObject jsonData) {
		this.jsonData = jsonData;
		action.setRequestData(jsonData);
		parseOperation();
		action.setOperation(operation);
	}

	private void parseOperation() {
		Operation[] operations = Operation.values();
		Set<String> keys = jsonData.keySet();
		int operationCount = 0;
		for (String key : keys) {
			for (Operation op : operations) {
				if (op.name().equalsIgnoreCase(key)) {
					operation = op;
					operationAttribute = key;
					operationCount++;
					break;
				}
			}
		}
		if (operationCount > 1) {
			throw new JsonDataException("Multiple operations were found in the Json data.");
		} else if (operationCount < 1) {
			throw new JsonDataException("No operations were found in the Json data.");
		}
	}

	public String parseSource() {
		String source = null;
		// 如果为事务操作, 则所有操作必须是同一个数据源
		if (isTransaction()) {
			JsonArray transactionArray = jsonData.getAsJsonArray(operationAttribute);
			Set<String> sources = new HashSet<>();
			for (JsonElement actionElement : transactionArray) {
				JsonObject actionObject = actionElement.getAsJsonObject();
				sources.add(new JsonParser(actionObject).parseSource());
			}
			if (sources.isEmpty() || sources.size() > 1) {
				throw new JsonDataException("Cross-data source transactions are not supported.");
			}
			source = sources.iterator().next();
		} else if (isStructs()) {
			source = jsonData.get(operationAttribute).getAsString();
		} else {
			JsonElement tablesElement = jsonData.get(operationAttribute);
			if (!tablesElement.isJsonObject() && !tablesElement.isJsonArray()) {
				String tableString = tablesElement.getAsString().trim();
				if (tableString.contains(AdditionalKeywords.ALIAS_KEY.value())) {
					String[] tablePart = tableString.split(AdditionalKeywords.ALIAS_KEY.value());
					tableString = tablePart[0];
				} else if (tableString.contains(" ")) {
					String[] tablePart = tableString.split("\\s+");
					if (tablePart.length == 2) {
						if (tablePart[0].matches(wordRegex) && tablePart[1].matches(wordRegex)) {
							tableString = tablePart[0];
						}
					}
				}
				if (tableString.contains(".")) {
					String[] sourceTable = tableString.split("\\.");
					source = sourceTable[0];
				}
			}
		}
		return source;
	}

	public Action parse() {
		if (isTransaction()) {
			return action;
		}
		if (isStructs()) {
			return action;
		}
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
		case NATIVE:
			parseNative();
			break;

		default:
			break;
		}

		parseResult();
		parseTemplate();
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
		parseValues();
	}

	public void parseUpdate() {
		parseValues();
		parseWhere();
	}

	public void parseDelete() {
		parseWhere();
	}

	/**
	 * 解析 table 节点, 可以是数组或字符串
	 */
	public void parseTables() {
		JsonElement tablesElement = jsonData.get(operationAttribute);
		if (tablesElement.isJsonObject()) {
			throw new JsonDataException("table node cannot be an object");
		} else if (tablesElement.isJsonArray()) {
			JsonArray tableArray = (JsonArray) tablesElement;
			tableArray.forEach(e -> action.addTableItem(parseTable(e)));
		} else {
			action.addTableItem(parseTable(tablesElement));
		}
	}

	/**
	 * 解析 table 元素
	 * 
	 * @param tableElement
	 * @return
	 */
	private TableItem parseTable(JsonElement tableElement) {
		String tableString = tableElement.getAsString().trim();
		String alias = null;
		if (tableString.contains(AdditionalKeywords.ALIAS_KEY.value())) {
			String[] tablePart = tableString.split(AdditionalKeywords.ALIAS_KEY.value());
			tableString = tablePart[0];
			alias = tablePart[1];
		} else if (tableString.contains(" ")) {
			String[] tablePart = tableString.split("\\s+");
			if (tablePart.length == 2) {
				if (tablePart[0].matches(wordRegex) && tablePart[1].matches(wordRegex)) {
					tableString = tablePart[0];
					alias = tablePart[1];
				}
			}
		}
		String tableName;
		if (tableString.contains(".")) {
			String[] tableSchema = tableString.split("\\.", 2);
			tableName = tableSchema[1];
		} else {
			tableName = tableString;
		}
		TableItem tableItem;
		Table table = App.Context.getTable(tableName);
		boolean customAlias = false;
		if (Strings.isNullOrEmpty(alias)) {
			alias = App.Action.getTableAlias(table);
		} else {
			customAlias = true;
		}
		if (table == null) {
			if (App.Context.dialect().validTableExists()) {
				throw new DataException(ResultStatus.DATASOURCE_TABLE_NOT_EXIST, tableName);
			}
			tableItem = new TableItem(tableName, alias, customAlias);
		} else {
			tableItem = new TableItem(table, alias, customAlias);
		}
		// SQL 语句不指定表别名
		if (isInsert() || isUpdate() || isDelete()) {
			tableItem.setAlias(null);
		}
		// 保存临时 Item
		tempTableItems.put(table, tableItem);
		return tableItem;
	}

	public void parseJoins() {
		if (!validAttribute(RequestKeyword.JOIN.lowerName())) {
			return;
		}
		List<JoinElement> joinElements = new ArrayList<>();
		JsonElement joinsElement = jsonData.get(RequestKeyword.JOIN.lowerName());
		if (joinsElement.isJsonArray()) {
			((JsonArray) joinsElement).forEach(e -> joinElements.add(parseJoinTable(e)));
		} else {
			joinElements.add(parseJoinTable(joinsElement));
		}
		buildJoin(joinElements);
	}

	public JoinElement parseJoinTable(JsonElement joinElement) {
		if (joinElement.isJsonArray()) {
			throw new JsonDataException("Join child node cannot be an array");
		} else if (joinElement.isJsonObject()) {
			JsonObject joinObject = joinElement.getAsJsonObject();
			String joinTypeString = joinObject.keySet().iterator().next();
			JoinType joinType = JoinType.from(joinTypeString);
			String joinTableName = joinObject.getAsJsonPrimitive(joinTypeString).getAsString();
			return parseJoin(joinTableName, joinType);
		} else {
			String joinTableName = joinElement.getAsString();
			// 默认关联类型 LEFT
			return parseJoin(joinTableName, JoinType.LEFT);
		}
	}

	public JoinElement parseJoin(String joinTableName, JoinType joinType) {
		Table joinTable = App.Context.getTable(joinTableName);
		if (joinTable == null) {
			throw new DataException(ResultStatus.DATASOURCE_TABLE_NOT_EXIST, joinTableName);
		}
		TableItem joinTableItem = new TableItem(joinTable, App.Action.getTableAlias(joinTable), false);
		// 保存临时 Item
		tempJoinTableItems.put(joinTable, joinTableItem);
		return new JoinElement(joinTableItem, joinType);
	}

	/**
	 * 找到主表与 join 表的关系
	 * 
	 * @param joinElements
	 */
	public void buildJoin(List<JoinElement> joinElements) {
		TableItem tableItem = action.getTableItems().get(0);
		Table table = tableItem.getTable();
		// 查找关联关系
		Set<Relationship> relationships = new LinkedHashSet<>();
		for (JoinElement joinElement : joinElements) {
			Table joinTable = joinElement.getJoinTableItem().getTable();
			// 1. 主表关联 join 表 (数据表配置文件中的配置)
			Set<Relationship> tempRelationships = App.Context.getRelationships(table, joinTable);
			if (tempRelationships.isEmpty()) {
				throw new DataException(String.format("Association relation for the join table [%s] was not found",
						joinTable.getName()));
			}
			relationships.addAll(tempRelationships);
		}
		for (Relationship relationship : relationships) {
			Column primaryColumn = relationship.getPrimaryColumn();
			Column foreignColumn = relationship.getForeignColumn();

			JoinType joinType = null;
			for (JoinElement joinElement : joinElements) {
				if (joinElement.getJoinTableItem().getTable() == foreignColumn.getTable()) {
					joinType = joinElement.getJoinType();
					break;
				}
			}
			if (joinType == null) {
				joinType = JoinType.LEFT;
			}
			List<ColumnItem> leftColumns = new ArrayList<>();
			List<ColumnItem> rightColumns = new ArrayList<>();

			ColumnItem primaryColumnItem = new ColumnItem(primaryColumn, getTableItem(primaryColumn.getTable()));
			ColumnItem foreignColumnItem = new ColumnItem(foreignColumn, getTableItem(foreignColumn.getTable()));
			leftColumns.add(primaryColumnItem);
			rightColumns.add(foreignColumnItem);

			action.addJoinItem(new JoinItem(leftColumns, rightColumns, joinType));
		}
	}

	public class JoinElement {
		Column joinColumn;
		TableItem joinTableItem;
		JoinType joinType;
		AssociationType associationType;
		// 主表名称集合. 如 JoinElement 的 joinTableItem 为 C, 请求中的 join 的关联关系为 A join B join C,
		// 那么 tables 为 { A, B}
		List<String> tables = new ArrayList<>();

		public JoinElement(TableItem joinTableItem, JoinType joinType) {
			this.joinTableItem = joinTableItem;
			this.joinType = joinType;
		}

		public void addTable(String table) {
			tables.add(table);
		}

		public TableItem getJoinTableItem() {
			return joinTableItem;
		}

		public void setJoinTableItem(TableItem joinTableItem) {
			this.joinTableItem = joinTableItem;
		}

		public JoinType getJoinType() {
			return joinType;
		}

		public void setJoinType(JoinType joinType) {
			this.joinType = joinType;
		}

		public AssociationType getAssociationType() {
			return associationType;
		}

		public void setAssociationType(AssociationType associationType) {
			this.associationType = associationType;
		}

		public List<String> getTables() {
			return tables;
		}

		public void setTables(List<String> tables) {
			this.tables = tables;
		}

	}

	/**
	 * 解析 fields 节点, 可以是数组或字符串. 如果是查询操作并且 json 中没有指定 fields 属性, 就查询所有列
	 */
	public void parseColumns() {
		if (!validAttribute(RequestKeyword.FIELDS.lowerName())) {
			if (isDetail() || isSelect()) {
				createAllColumns();
			}
			return;
		}
		JsonElement columnsElement = jsonData.get(RequestKeyword.FIELDS.lowerName());
		if (columnsElement.isJsonObject()) {
			throw new JsonDataException("fields node cannot be an object");
		} else if (columnsElement.isJsonArray()) {
			((JsonArray) columnsElement).forEach(e -> action.addColumnItem(parseColumn(e)));
		} else {
			action.addColumnItem(parseColumn(columnsElement));
		}
	}

	/**
	 * 解析 fields 节点
	 * 
	 * @param columnElement
	 * @return
	 */
	private ColumnItem parseColumn(JsonElement columnElement) {
		String columnString = columnElement.getAsString().trim();
		String alias = null;
		if (columnString.contains(AdditionalKeywords.ALIAS_KEY.value())) {
			String[] columnPart = columnString.split(AdditionalKeywords.ALIAS_KEY.value());
			columnString = columnPart[0];
			alias = columnPart[1];
		} else if (columnString.contains(" ")) {
			String columnRegex = "^(?![0-9]*$)[a-zA-Z0-9_$()]+$";
			String aliasRegex = "^(?![0-9]*$)[a-zA-Z0-9_$']+$";
			String[] columnPart = columnString.split("\\s+");
			if (columnPart.length == 2) {
				if (columnPart[0].matches(columnRegex) && columnPart[1].matches(aliasRegex)) {
					columnString = columnPart[0];
					alias = columnPart[1];
				}
			} else {
				int lastSpaceIndex = columnString.lastIndexOf(" ");
				String frontPart = columnString.substring(0, lastSpaceIndex + 1).trim();
				String backPart = columnString.substring(lastSpaceIndex + 1).trim();
				if (backPart.matches(aliasRegex)) {
					columnString = frontPart;
					alias = backPart;
					if (alias.startsWith("'") && alias.endsWith("'")) {
						alias = alias.split("'")[1];
					}
				}
			}
		}
		// 所有列. 例: "fields": "*"
		if ("*".equals(columnString)) { // 所有列
			createAllColumns();
			return null;
		}
		// 指定表的所有列. 例: "fields": "table.*", "fields": "datasource.table.*"
		if (".*".equals(columnString)) {
			String tableName = null;
			long dotCount = columnString.chars().filter(ch -> ch == '.').count();
			String[] columnParts = columnString.split("\\.");
			if (dotCount == 2) { // schema.table.column
				// String schemaName = columnParts[0];
				tableName = columnParts[1];
			} else if (dotCount == 1) { // table.column
				tableName = columnParts[0];
			}
			createTableColumns(tableName);
			return null;
		}
		// 单列
		return parseColumn(columnString, alias);
	}

	private ColumnItem parseColumn(String columnString, String alias) {
		return createColumnItem(columnString, alias);
	}

	/**
	 * 添加所有表(主表和 join 表)的所有列
	 */
	private void createAllColumns() {
		createMainColumns();
		createJoinColumns();
	}

	/**
	 * 添加主表的所有列
	 */
	private void createMainColumns() {
		tempTableItems.values().stream().forEach(this::createMainTableItemColumns);
	}

	/**
	 * 添加 join 表的所有列
	 */
	private void createJoinColumns() {
		tempJoinTableItems.values().stream().forEach(this::createJoinTableItemColumns);
	}

	/**
	 * 添加指定表的所有列
	 * 
	 * @param tableName
	 */
	private void createTableColumns(String tableName) {
		if (tableName == null) {
			return;
		}
		List<TableItem> tableItems = action.getTableItems();
		for (TableItem tableItem : tableItems) {
			Table table = tableItem.getTable();
			if (table.getName().equals(tableName)) {
				createMainTableItemColumns(tableItem);
				return;
			}
		}
		List<JoinItem> joinItems = action.getJoinItems();
		for (JoinItem joinItem : joinItems) {
			TableItem joinTableItem = joinItem.getRightColumns().get(0).getTableItem();
			Table joinTable = joinTableItem.getTable();
			if (joinTable.getName().equals(tableName)) {
				createJoinTableItemColumns(joinTableItem);
				return;
			}
		}
	}

	private void createMainTableItemColumns(TableItem tableItem) {
		Table table = tableItem.getTable();
		String expression = tableItem.getExpression();
		if (table != null) {
			List<Column> columns = table.getColumns();
			for (Column column : columns) {
				action.addColumnItem(new ColumnItem(column, App.Action.getColumnAlias(column), false, tableItem));
			}

		} else if (!Strings.isNullOrEmpty(expression)) {
			action.addColumnItem(new ColumnItem(SQLBuilder.COLUMN_ALL));
		}
	}

	/**
	 * 添加指定操作表的所有列
	 * 
	 * @param tableItem
	 */
	private void createJoinTableItemColumns(TableItem tableItem) {
		List<Column> columns = tableItem.getTable().getColumns();
		for (Column column : columns) {
			JoinColumnItem joinColumnItem = new JoinColumnItem(column, App.Action.getColumnAlias(column), false,
					tableItem);
			parseJoinColumnAssociation(joinColumnItem);
			action.addColumnItem(joinColumnItem);
		}
	}

	/**
	 * 查找关联表的列的关联信息, 用于结果渲染时的关联表结构创建
	 * 
	 * @param joinColumnItem
	 */
	private void parseJoinColumnAssociation(JoinColumnItem joinColumnItem) {
		Table joinTable = joinColumnItem.getTableItem().getTable();
		over: for (List<Table> tables : tempJoins) {
			for (int i = tables.size() - 1; i > 0; i--) {
				if (joinTable.getName().equals(tables.get(i).getName())) {
					// 添加所有父级关联表
					for (int j = 0; j < i; j++) {
						joinColumnItem.addParentTable(tables.get(j));
					}
					break over;
				}
			}
		}
	}

	/**
	 * 解析 where 节点, 可以是数组, 对象, 或字符串
	 */
	public void parseWhere() {
		if (!validAttribute(RequestKeyword.WHERE.lowerName())) {
			return;
		}
		JsonElement whereElement = jsonData.get(RequestKeyword.WHERE.lowerName());
		if (whereElement.isJsonArray()) {
			((JsonArray) whereElement).forEach(f -> action.addFilterItem(parseFilter(f)));
		} else if (whereElement.isJsonObject()) {
			Entry<String, JsonElement> objectFilter = ((JsonObject) whereElement).entrySet().iterator().next();
			String connectorString = objectFilter.getKey();
			JsonElement objectInnerFilter = objectFilter.getValue();
			FilterItem parsedFilter = parseFilter(objectInnerFilter);
			parsedFilter.setConnector(Connector.from(connectorString));
			action.addFilterItem(parsedFilter);
		} else {
			action.addFilterItem(parseFilter(whereElement));
		}
	}

	/**
	 * 解析 where 单个条件对象. 可以是数组,对象,字符串.
	 * 
	 * @param filterElement
	 * @return
	 */
	private FilterItem parseFilter(JsonElement filterElement) {
		FilterItem filterItem = new FilterItem();
		if (filterElement.isJsonArray()) {
			for (JsonElement innerFilterItem : (JsonArray) filterElement) {
				filterItem.addSubFilterItem(parseFilter(innerFilterItem));
			}
			return filterItem;
		} else if (filterElement.isJsonObject()) {
			Entry<String, JsonElement> objectFilter = ((JsonObject) filterElement).entrySet().iterator().next();
			String connectorString = objectFilter.getKey();
			JsonElement objectInnerFilter = objectFilter.getValue();
			if (objectInnerFilter.isJsonArray()) {
				for (JsonElement innerFilterItem : (JsonArray) objectInnerFilter) {
					filterItem.addSubFilterItem(parseFilter(innerFilterItem));
				}
			} else if (objectInnerFilter.isJsonPrimitive()) {
				filterItem = parseFilter(objectInnerFilter);
			} else {
				throw new JsonDataException("where node format error");
			}
			filterItem.setConnector(Connector.from(connectorString));
			return filterItem;
		} else {
			String filterString = filterElement.getAsString().trim();
			return parseFilter(filterString);
		}
	}

	/**
	 * 解析 where 字符串形式的条件. 如 "a=1"
	 * 
	 * @param filterString
	 * @return
	 */
	private FilterItem parseFilter(String filterString) {
		Cond cond = parseCond(filterString);
		Operator operator = cond.getOperator();
		String columnString = cond.getColumn();
		Object value = cond.getValue();

		ColumnItem columnItem = findColumnItem(columnString);
		return new FilterItem(columnItem, value, Connector.AND, operator);
	}

	/**
	 * 解析条件运算符
	 * 
	 * @param filterString
	 * @return
	 */
	private Cond parseCond(String filterString) {
		Operator operator = null;
		int pos = 0;
		int length = filterString.length();
		over: while (pos < length) {
			switch (filterString.charAt(pos++)) {
			case '=':
				switch (filterString.charAt(pos++)) {
				case '=':
					operator = Operator.STRONG_EQUAL;
					break over;
				default:
					break;
				}

				String tail = filterString.substring(pos);
				if (tail.contains(",")) { // in
					operator = Operator.IN;
				} else if (tail.contains("~")) { // between
					operator = Operator.BETWEEN;
				} else { // 等于
					operator = Operator.EQUAL;
				}
				break over;
			case '!':
				switch (filterString.charAt(pos++)) {
				case '=':
					String notTail = filterString.substring(pos);
					if (notTail.contains(",")) { // in
						operator = Operator.NOT_IN;
					} else { // 等于
						operator = Operator.NOT_EQUAL;
					}
					break over;

				case '%':
					switch (filterString.charAt(pos++)) {
					case '=':
						operator = Operator.NOT_LIKE;
						break over;
					default:
						break;
					}
					break over;

				default:
					break;
				}
				break;
			case '>':
				switch (filterString.charAt(pos++)) {
				case '=':
					operator = Operator.GTE;
					break over;
				default:
					operator = Operator.GT;
				}
				break;
			case '<':
				switch (filterString.charAt(pos++)) {
				case '=':
					operator = Operator.LTE;
					break over;
				default:
					operator = Operator.LT;
				}
				break;
			case '%':
				switch (filterString.charAt(pos++)) {
				case '=':
					operator = Operator.LIKE;
					break over;
				default:
					break;
				}
				break;

			default:
				break;
			}
		}
		if (operator == null) {
			return null;
		}

		String[] kv = null;
		/*
		 * 对 in 和 between 做特殊处理, 待优化
		 */
		switch (operator) {
		case IN:
		case BETWEEN:
			kv = filterString.split(Operator.EQUAL.op(), 2);
			break;
		case NOT_IN:
			kv = filterString.split(Operator.NOT_EQUAL.op(), 2);
			break;

		default:
			kv = filterString.split(operator.op(), 2);
			break;
		}

		String column = kv[0];
		Object value = kv[1];
		// 多值条件时, 将多值字符串转换为数组
		if (operator == Operator.IN || operator == Operator.NOT_IN || operator == Operator.BETWEEN) {
			value = value.toString().split(operator.op());
		}

		return new Cond(operator, column, value);
	}

	private class Cond {

		private Operator operator;

		String column;

		Object value;

		public Cond(Operator operator, String column, Object value) {
			this.operator = operator;
			this.column = column;
			this.value = value;
		}

		public Operator getOperator() {
			return operator;
		}

		public String getColumn() {
			return column;
		}

		public Object getValue() {
			return value;
		}

	}

	/**
	 * 解析 Group 节点, 可以是数组或字符串
	 */
	public void parseGroups() {
		if (!validAttribute(RequestKeyword.GROUP.lowerName())) {
			return;
		}
		JsonElement groupsElement = jsonData.get(RequestKeyword.GROUP.lowerName());
		if (groupsElement.isJsonObject()) {
			throw new JsonDataException("group node cannot be an object");
		} else if (groupsElement.isJsonArray()) {
			((JsonArray) groupsElement).forEach(e -> action.addGroupItem(parseGroup(e.getAsString().trim())));
		} else {
			String groupString = groupsElement.getAsString().trim();
			action.addGroupItem(parseGroup(groupString));
		}
	}

	/**
	 * 解析 Group 元素
	 * 
	 * @param groupString
	 * @return GroupItem
	 */
	public GroupItem parseGroup(String groupString) {
		ColumnItem columnItem = findColumnItem(groupString);
		return new GroupItem(columnItem);
	}

	/**
	 * 解析 Order 节点, 可以是数组或字符串
	 */
	public void parseOrders() {
		if (!validAttribute(RequestKeyword.ORDER.lowerName())) {
			return;
		}
		JsonElement ordersElement = jsonData.get(RequestKeyword.ORDER.lowerName());
		if (ordersElement.isJsonObject()) {
			throw new JsonDataException("order node cannot be an object");
		} else if (ordersElement.isJsonArray()) {
			((JsonArray) ordersElement).forEach(e -> action.addOrderItem(parseOrder(e.getAsString().trim())));
		} else {
			String orderString = ordersElement.getAsString().trim();
			action.addOrderItem(parseOrder(orderString));
		}
	}

	/**
	 * 解析 Order 元素, 分为2种格式：1. "column desc", 2. "+column". 默认升序
	 * 
	 * @param orderString
	 * @return OrderItem
	 */
	public OrderItem parseOrder(String orderString) {
		String columnString = null;
		Order order = null;
		if (orderString.endsWith(" asc") || orderString.endsWith(" desc")) {
			String[] columnOrder = orderString.split("\\s+");
			columnString = columnOrder[0];
			String orderTypeString = columnOrder[1];
			order = orderTypeString.equals("desc") ? Order.DESC : Order.ASC;
		} else if (orderString.startsWith("+") || orderString.startsWith("-")) {
			String symbol = orderString.substring(0, 1);
			columnString = orderString.substring(1);
			order = symbol.equals("-") ? Order.DESC : Order.ASC;
		} else {
			columnString = orderString;
			order = Order.ASC;
		}
		ColumnItem columnItem = findColumnItem(columnString);
		return new OrderItem(columnItem, order);
	}

	public void parseLimit() {
		if (!validAttribute(RequestKeyword.LIMIT.lowerName())) {
			return;
		}
		JsonElement limitElement = jsonData.get(RequestKeyword.LIMIT.lowerName());
		if (!limitElement.isJsonArray()) {
			throw new JsonDataException("limit node must be an array");
		} else {
			JsonArray limitArray = (JsonArray) limitElement;
			long start = limitArray.get(0).getAsLong();
			long end = limitArray.get(1).getAsLong();
			action.addLimitItem(new LimitItem(start, end));
		}
	}

	public void parseValues() {
		if (!validAttribute(RequestKeyword.VALUES.lowerName())) {
			return;
		}
		JsonElement valuesElement = jsonData.get(RequestKeyword.VALUES.lowerName());
		if (!valuesElement.isJsonObject()) {
			throw new JsonDataException("values node must be an object");
		} else {
			JsonObject values = (JsonObject) valuesElement;
			for (String columnName : values.keySet()) {
				Column column = findColumn(columnName);
				if (column != null) {
					JsonElement valueElement = values.get(columnName);
					String value = null;
					if (valueElement.isJsonNull()) {
						//
					} else if (valueElement.isJsonPrimitive()) {
						value = valueElement.getAsString();
					} else {
						value = valueElement.toString();
					}
					action.addValueItem(new ValueItem(column, value));
				}
			}
		}
	}

	private ColumnItem createColumnItem(String columnString) {
		return createColumnItem(columnString, "");
	}

	/**
	 * 创建 ColumnItem
	 * 
	 * @param columnString
	 *            列表达式, 可能是 schema.table.column, table.column, column, function() 等
	 * @param alias
	 *            列别名
	 * @return
	 */
	private ColumnItem createColumnItem(String columnString, String alias) {
		boolean customAlias = false;
		if (Strings.isNullOrEmpty(alias)) {
			alias = App.Action.getAlias(null);
		} else {
			customAlias = true;
		}
		Column column = findColumn(columnString);
		if (column == null) { // 不是列名的情况, 如函数
			return new ColumnItem(columnString, alias, customAlias);
		} else {
			TableItem tableItem = getMainTableItem(column.getTable());
			if (tableItem != null) {
				return new ColumnItem(column, alias, customAlias, tableItem);
			} else {
				tableItem = getJoinTableItem(column.getTable());
				if (tableItem != null) {
					JoinColumnItem joinColumnItem = new JoinColumnItem(column, alias, customAlias, tableItem);
					parseJoinColumnAssociation(joinColumnItem);
					return joinColumnItem;
				}
			}
		}
		return null;
	}

	/**
	 * 根据字段表达式查找字段
	 * 
	 * @param columnString
	 *            字段字符串
	 * @return
	 */
	private Column findColumn(String columnString) {
		long dotCount = columnString.chars().filter(ch -> ch == '.').count();
		String[] columnParts = columnString.split("\\.");
		if (dotCount == 2) { // schema.table.column
			String schemaName = columnParts[0];
			String tableName = columnParts[1];
			String columnName = columnParts[2];
			return App.Context.getColumn(schemaName, tableName, columnName);
		} else if (dotCount == 1) { // table.column
			String tableName = columnParts[0];
			String columnName = columnParts[1];
			return App.Context.getColumn(tableName, columnName);
		} else { // column
			return findColumnByName(columnString);
		}
	}

	/**
	 * 根据列名在操作表中查找列
	 * 
	 * @param columnString
	 * @return
	 */
	private Column findColumnByName(String columnString) {
		Column column = findMainColumn(columnString);
		if (column == null) {
			column = findJoinColumn(columnString);
		}
		return column;
	}

	/**
	 * 在操作主表中查找字段信息
	 * 
	 * @param columnString
	 * @return
	 */
	private Column findMainColumn(String columnString) {
		for (TableItem tableItem : action.getTableItems()) {
			Column column = App.Context.getColumn(tableItem.getTable(), columnString);
			if (column != null) {
				return column;
			}
		}
		return null;
	}

	/**
	 * 在 join 表中查找字段信息
	 * 
	 * @param columnString
	 * @return
	 */
	private Column findJoinColumn(String columnString) {
		for (JoinItem joinItem : action.getJoinItems()) {
			List<Column> primaryTableColumns = joinItem.getLeftColumns().get(0).getTableItem().getTable().getColumns();
			List<Column> foreignTableColumns = joinItem.getRightColumns().get(0).getTableItem().getTable().getColumns();
			Optional<Column> primaryColumn = primaryTableColumns.stream().filter(c -> c.getName().equals(columnString))
					.findFirst();
			if (primaryColumn.isPresent()) {
				return primaryColumn.get();
			}
			Optional<Column> foreignColumn = foreignTableColumns.stream().filter(c -> c.getName().equals(columnString))
					.findFirst();
			if (foreignColumn.isPresent()) {
				return foreignColumn.get();
			}

		}
		return null;
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
		} else if (tempJoinTableItems.containsKey(table)) {
			tableItem = tempJoinTableItems.get(table);
		} else if (tempRelationTableItems.containsKey(table)) {
			tableItem = tempRelationTableItems.get(table);
		} else {
			tableItem = new TableItem(table, App.Action.getTableAlias(table), false);
			tempRelationTableItems.put(table, tableItem);
		}
		return tableItem;
	}

	/**
	 * 获取主表的 TableItem
	 * 
	 * @param table
	 * @return
	 */
	private TableItem getMainTableItem(Table table) {
		for (TableItem tableItem : action.getTableItems()) {
			Table mainTable = tableItem.getTable();
			if (mainTable == table) {
				return tableItem;
			}
		}
		return null;
	}

	/**
	 * 获取 join 表的 TableItem
	 * 
	 * @param table
	 * @return
	 */
	private TableItem getJoinTableItem(Table table) {
		for (JoinItem joinItem : action.getJoinItems()) {
			TableItem tableItem = joinItem.getRightColumns().get(0).getTableItem();
			Table joinTable = tableItem.getTable();
			if (joinTable == table) {
				return tableItem;
			}
		}
		return null;
	}

	/**
	 * 根据列别名或列名在已经解析过的列数据中查找 ColumnItem, 如果没有就新创建一个 ColumnItem
	 * 
	 * @param columnString
	 * @return
	 */
	private ColumnItem findColumnItem(String columnString) {
		Column findColumn = findColumn(columnString);
		// 根据列别名查找
		for (ColumnItem columnItem : action.getColumnItems()) {
			Column column = columnItem.getColumn();
			String columnName = column == null ? "" : column.getName();
			if (columnString.equals(columnItem.getAlias()) || columnString.equalsIgnoreCase(columnName)
					|| (column != null && column == findColumn)) {
				return columnItem;
			}
		}
		return createColumnItem(columnString);
	}

	public void parseNative() {
		if (!validAttribute(RequestKeyword.NATIVE.lowerName())) {
			return;
		}
		String nativeContent = jsonData.get(RequestKeyword.NATIVE.lowerName()).getAsString();
		action.setNativeContent(nativeContent);
	}

	private void parseResult() {
		if (!validAttribute(RequestKeyword.RESULT.lowerName())) {
			return;
		}
		String resultString = jsonData.get(RequestKeyword.RESULT.lowerName()).getAsString();
		ResultType resultType = ResultType.from(resultString);
		action.setResultType(resultType);
	}

	private void parseTemplate() {
		if (!validAttribute(RequestKeyword.TEMPLATE.lowerName())) {
			return;
		}
		String templateString = jsonData.get(RequestKeyword.TEMPLATE.lowerName()).getAsString();
		Template template = Template.from(templateString);
		action.setTemplate(template);
	}

	private void finish() {
		action.setTables(new ArrayList<>(tempTableItems.keySet()));
		action.setJoinTables(new ArrayList<>(tempJoinTableItems.keySet()));
	}

	private boolean validAttribute(String attribute) {
		if (jsonData.has(attribute)) {
			JsonElement attributeElement = jsonData.get(attribute);
			if (attributeElement.isJsonNull()) {
				throw new JsonDataException(attribute + " node value is null");
			}
			return true;
		}
		return false;
	}

	public boolean isDetail() {
		return operation != null && operation == Operation.DETAIL;
	}

	public boolean isSelect() {
		return operation != null && (operation == Operation.QUERY || operation == Operation.SELECT);
	}

	public boolean isUpdate() {
		return operation != null && operation == Operation.UPDATE;
	}

	public boolean isInsert() {
		return operation != null && operation == Operation.INSERT;
	}

	public boolean isDelete() {
		return operation != null && operation == Operation.DELETE;
	}

	public boolean isTransaction() {
		return operation != null && operation == Operation.TRANSACTION;
	}

	public boolean isStruct() {
		return operation != null && (operation == Operation.STRUCT);
	}

	public boolean isStructs() {
		return operation != null && (operation == Operation.STRUCTS);
	}

	public boolean isNative() {
		return operation != null && operation == Operation.NATIVE;
	}

	public boolean isResultFile() {
		return jsonData.has(RequestKeyword.RESULT.lowerName());
	}

	public boolean isTemplate() {
		return jsonData.has(RequestKeyword.TEMPLATE.lowerName());
	}

	public Operation getOperation() {
		return operation;
	}

	public Action getAction() {
		return action;
	}

	public JsonObject getJsonData() {
		return jsonData;
	}

}

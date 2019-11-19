package com.github.mengxianun.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.JsonDataException;
import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.parser.info.ConditionInfo;
import com.github.mengxianun.core.parser.info.FileInfo;
import com.github.mengxianun.core.parser.info.FilterInfo;
import com.github.mengxianun.core.parser.info.GroupInfo;
import com.github.mengxianun.core.parser.info.JoinInfo;
import com.github.mengxianun.core.parser.info.LimitInfo;
import com.github.mengxianun.core.parser.info.NativeInfo;
import com.github.mengxianun.core.parser.info.OrderInfo;
import com.github.mengxianun.core.parser.info.RelationInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.SourceInfo;
import com.github.mengxianun.core.parser.info.SqlInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.parser.info.ValuesInfo;
import com.github.mengxianun.core.parser.info.WhereInfo;
import com.github.mengxianun.core.request.AdditionalKeywords;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.JoinType;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.Order;
import com.github.mengxianun.core.request.RequestKeyword;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SimpleParser {

	private static final Pattern SOURCE_TABLE_COLUMN = Pattern
			.compile("((?<source>[\\w-]*)(\\.)+)*(?<table>[\\w-]+)\\.(?<column>([\\w-]+|\\*))");
	public static final String MATCHER_GROUP_SOURCE = "source";
	public static final String MATCHER_GROUP_TABLE = "table";
	public static final String MATCHER_GROUP_COLUMN = "column";
	private static final Gson GSON = new Gson();
	private final JsonObject jsonData;
	private Operation operation;
	private String operationAttribute;

	private SimpleInfo.Builder builder;

	public SimpleParser(String json) {
		this(GSON.fromJson(json, JsonObject.class));
	}

	public SimpleParser(JsonObject jsonData) {
		this.jsonData = jsonData;
		builder = SimpleInfo.builder();
	}

	private SimpleInfo parse() {
		parseOperation();
		switch (operation) {
		case STRUCT:
			parseStruct();
			break;
		case STRUCTS:
			parseStructs();
			break;
		case QUERY:
		case SELECT:
		case SELECT_DISTINCT:
		case DETAIL:
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
		case SQL:
			parseSql();
			break;
		case NATIVE:
			parseNative();
			break;
		case TRANSACTION:
			parseTransaction();
			break;

		default:
			break;
		}
		return builder.build();
	}

	private void parseOperation() {
		Operation[] operations = Operation.values();
		Set<String> keys = jsonData.keySet();
		int operationCount = 0;
		for (String key : keys) {
			for (Operation op : operations) {
				if (op.value().equalsIgnoreCase(key)) {
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
		builder.operation(operation);
	}

	/**
	 * Parse source and table from a string.
	 * 
	 * @param sourceTableString
	 * @return
	 */
	private TableInfo parseSourceTable(String tableString) {
		String source = null;
		String table = null;
		String alias = null;
		if (tableString.contains(AdditionalKeywords.ALIAS_KEY.value())) {
			String[] tablePart = tableString.split(AdditionalKeywords.ALIAS_KEY.value());
			tableString = tablePart[0];
			alias = tablePart[1];
		}
		if (tableString.contains(".")) {
			String[] tableSource = tableString.split("\\.", 2);
			source = tableSource[0];
			table = tableSource[1];
		} else {
			table = tableString;
		}
		return TableInfo.create(source, table, alias);
	}

	private void parseStruct() {
		parsePrimaryTable();
	}

	private void parseStructs() {
		String source = jsonData.get(operationAttribute).getAsString();
		SourceInfo sourceInfo = SourceInfo.create(source);
		builder.source(sourceInfo);
	}

	private void parseSelect() {
		parsePrimaryTable();
		parseJoin();
		parseRelations();
		parseFields();
		parseWhere();
		parseGroup();
		parseOrder();
		parseLimit();
		parseFile();
	}

	private void parsePrimaryTable() {
		String sourceTableString = jsonData.get(operationAttribute).getAsString().trim();
		TableInfo tableInfo = parseSourceTable(sourceTableString);
		builder.table(tableInfo);
	}

	private void parseJoin() {
		if (!validAttribute(RequestKeyword.JOIN.lowerName())) {
			return;
		}
		List<JoinInfo> joins = new ArrayList<>();
		JsonElement joinElements = jsonData.get(RequestKeyword.JOIN.lowerName());
		if (joinElements.isJsonArray()) {
			((JsonArray) joinElements).forEach(e -> addNotNull(joins, parseJoin(e)));
		} else {
			addNotNull(joins, parseJoin(joinElements));
		}
		builder.joins(joins);
	}

	private JoinInfo parseJoin(JsonElement joinElement) {
		if (joinElement.isJsonArray()) {
			throw new JsonDataException("Join child node cannot be an array");
		} else if (joinElement.isJsonObject()) {
			JsonObject joinObject = joinElement.getAsJsonObject();
			String joinTypeString = joinObject.keySet().iterator().next();
			JoinType joinType = JoinType.from(joinTypeString);
			if (joinType == null) {
				throw new DataException("Unsupported join type[%s]", joinTypeString);
			}
			String joinSourceTableString = joinObject.getAsJsonPrimitive(joinTypeString).getAsString();
			return createJoin(joinType, joinSourceTableString);
		} else {
			String joinSourceTableString = joinElement.getAsString();
			return createJoin(JoinType.LEFT, joinSourceTableString);
		}
	}

	private JoinInfo createJoin(JoinType joinType, String joinSourceTableString) {
		if (Strings.isNullOrEmpty(joinSourceTableString)) {
			return null;
		}
		TableInfo joinTableInfo = parseSourceTable(joinSourceTableString);
		return JoinInfo.create(joinType, joinTableInfo);
	}

	private void parseRelations() {
		if (!validAttribute(RequestKeyword.RELATIONS.lowerName())) {
			return;
		}
		List<RelationInfo> relationInfos = new ArrayList<>();
		JsonElement relationElements = jsonData.get(RequestKeyword.RELATIONS.lowerName());
		if (relationElements.isJsonArray()) {
			for (JsonElement relationElement : (JsonArray) relationElements) {
				RelationInfo relationInfo = parseRelation(relationElement);
				if (relationInfo != null) {
					relationInfos.add(relationInfo);
				}
			}
		} else if (relationElements.isJsonPrimitive()) {
			relationInfos.add(parseRelation(relationElements.getAsString()));
		}
		builder.relations(relationInfos);
	}

	private RelationInfo parseRelation(JsonElement relationElement) {
		if (relationElement.isJsonArray()) {
			// to do
		} else if (relationElement.isJsonObject()) {
			// to do
		} else if (relationElement.isJsonPrimitive()) {
			return parseRelation(relationElement.getAsString());
		}
		return null;
	}

	private RelationInfo parseRelation(String relationString) {
		String[] tables = relationString.split("=", 2);

		String[] primaryTableColumn = tables[0].split("\\.", 2);
		String primaryTable = primaryTableColumn[0];
		String primaryColumn = primaryTableColumn[1];

		String[] foreignTableColumn = tables[1].split("\\.", 2);
		String foreignTable = foreignTableColumn[0];
		String foreignColumn = foreignTableColumn[1];
		return RelationInfo.create(primaryTable, primaryColumn, foreignTable, foreignColumn);
	}

	private void parseFields() {
		if (!validAttribute(RequestKeyword.FIELDS.lowerName())) {
			return;
		}
		List<ColumnInfo> columnInfos = new ArrayList<>();
		JsonElement fieldsElements = jsonData.get(RequestKeyword.FIELDS.lowerName());
		if (fieldsElements.isJsonArray()) {
			((JsonArray) fieldsElements).forEach(e -> addNotNull(columnInfos, parseColumn(e.getAsString())));
		} else {
			addNotNull(columnInfos, parseColumn(fieldsElements.getAsString()));
		}
		builder.columns(columnInfos);
	}

	private ColumnInfo parseColumn(String fieldString) {
		if (Strings.isNullOrEmpty(fieldString)) {
			return null;
		}
		String source = null;
		String table = null;
		String column = null;
		String alias = null;
		fieldString = fieldString.trim();
		if (fieldString.contains(AdditionalKeywords.ALIAS_KEY.value())) {
			String[] parts = fieldString.split(AdditionalKeywords.ALIAS_KEY.value(), 2);
			fieldString = parts[0];
			alias = parts[1];
		}

		Matcher matcher = SOURCE_TABLE_COLUMN.matcher(fieldString);
		if (matcher.matches()) {
			source = matcher.group(MATCHER_GROUP_SOURCE);
			table = matcher.group(MATCHER_GROUP_TABLE);
			column = matcher.group(MATCHER_GROUP_COLUMN);
		} else {
			column = fieldString;
		}
		return ColumnInfo.create(source, table, column, alias);
	}

	private void parseWhere() {
		if (!validAttribute(RequestKeyword.WHERE.lowerName())) {
			return;
		}
		List<FilterInfo> filters = new ArrayList<>();
		JsonElement whereElement = jsonData.get(RequestKeyword.WHERE.lowerName());
		if (whereElement.isJsonArray()) {
			((JsonArray) whereElement).forEach(e -> filters.add(parseFilter(e)));
		} else {
			filters.add(parseFilter(whereElement));
		}
		builder.where(WhereInfo.create(filters));
	}

	private FilterInfo parseFilter(JsonElement filterElement) {
		if (filterElement.isJsonArray()) {
			List<FilterInfo> subFilterInfos = new ArrayList<>();
			for (JsonElement innerFilterInfoItem : (JsonArray) filterElement) {
				subFilterInfos.add(parseFilter(innerFilterInfoItem));
			}
			return FilterInfo.create(Connector.AND, null, subFilterInfos);
		} else if (filterElement.isJsonObject()) {
			Entry<String, JsonElement> objectFilterInfo = ((JsonObject) filterElement).entrySet().iterator().next();
			String connectorString = objectFilterInfo.getKey();
			Connector connector = Connector.from(connectorString);
			JsonElement objectInnerFilterInfo = objectFilterInfo.getValue();
			if (objectInnerFilterInfo.isJsonArray()) {
				List<FilterInfo> subFilterInfos = new ArrayList<>();
				for (JsonElement innerFilterInfoItem : (JsonArray) objectInnerFilterInfo) {
					subFilterInfos.add(parseFilter(innerFilterInfoItem));
				}
				return FilterInfo.create(connector, null, subFilterInfos);
			} else if (objectInnerFilterInfo.isJsonPrimitive()) {
				String filterString = objectInnerFilterInfo.getAsString().trim();
				ConditionInfo conditionInfo = parseCondition(filterString);
				return FilterInfo.create(connector, conditionInfo, Collections.emptyList());
			} else {
				throw new JsonDataException("where node format error");
			}
		} else {
			String filterString = filterElement.getAsString().trim();
			ConditionInfo conditionInfo = parseCondition(filterString);
			return FilterInfo.create(Connector.AND, conditionInfo, Collections.emptyList());
		}
	}

	public ConditionInfo parseCondition(String filterString) {
		Operator operator = parseOperator(filterString);
		if (operator == null) {
			return null;
		}

		String[] kv = filterString.split(operator.op(), 2);
		String column = kv[0];
		Object value = kv[1];
		String stringValue = value.toString().trim();

		switch (operator) {
		case EQUAL:
			if (stringValue.contains(",")) { // in
				operator = Operator.IN;
				value = stringValue.split(",");
			} else if (stringValue.contains("~")) { // between
				operator = Operator.BETWEEN;
				value = stringValue.split("~");
			} else if ("null".equalsIgnoreCase(stringValue)) {
				operator = Operator.NULL;
			}
			break;
		case NOT_EQUAL:
			if (stringValue.contains(",")) { // in
				operator = Operator.NOT_IN;
				value = stringValue.split(",");
			} else if (stringValue.contains("~")) { // between
				operator = Operator.NOT_BETWEEN;
				value = stringValue.split("~");
			} else if ("null".equalsIgnoreCase(stringValue)) {
				operator = Operator.NOT_NULL;
			}
			break;
		case IN:
		case NOT_IN:
		case BETWEEN:
		case NOT_BETWEEN:
			// 多值条件时, 将多值字符串转换为数组
			value = stringValue.split(operator.separator());
			break;
		case STRONG_EQUAL:
			operator = Operator.EQUAL;
			value = value.toString();
			break;
		case NOT_STRONG_EQUAL:
			operator = Operator.NOT_EQUAL;
			value = value.toString();
			break;

		default:
			break;
		}

		ColumnInfo columnInfo = parseColumn(column);
		return ConditionInfo.create(columnInfo, operator, value);
	}

	private Operator parseOperator(String filterString) {
		Operator operator = null;
		int pos = 0;
		int length = filterString.length();
		over: while (pos < length) {
			switch (filterString.charAt(pos++)) {
			case '=':
				if (filterString.charAt(pos) == '=') {
					operator = Operator.STRONG_EQUAL;
					break over;
				}
				operator = Operator.EQUAL;

				break over;
			case '!':
				switch (filterString.charAt(pos++)) {
				case '=':
					if (filterString.charAt(pos) == '=') {
						operator = Operator.NOT_STRONG_EQUAL;
						break over;
					}
					operator = Operator.NOT_EQUAL;

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
				case '~':
					switch (filterString.charAt(pos++)) {
					case '=':
						operator = Operator.NOT_BETWEEN;
						break over;
					default:
						break;
					}
					break over;
				case ',':
					switch (filterString.charAt(pos++)) {
					case '=':
						if (filterString.charAt(pos) == '=') {
							operator = Operator.NOT_IN_SQL;
							break over;
						}
						operator = Operator.NOT_IN;
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
			case ',':
				switch (filterString.charAt(pos++)) {
				case '=':
					if (filterString.charAt(pos) == '=') {
						operator = Operator.IN_SQL;
						break over;
					}
					operator = Operator.IN;
					break over;
				default:
					break;
				}
				break;

			default:
				break;
			}
		}
		return operator;
	}

	private void parseGroup() {
		if (!validAttribute(RequestKeyword.GROUP.lowerName())) {
			return;
		}
		List<GroupInfo> groups = new ArrayList<>();
		JsonElement groupElement = jsonData.get(RequestKeyword.GROUP.lowerName());
		if (groupElement.isJsonObject()) {
			throw new JsonDataException("Group node cannot be an object");
		} else if (groupElement.isJsonArray()) {
			((JsonArray) groupElement).forEach(e -> addNotNull(groups, parseGroup(e.getAsString())));
		} else {
			addNotNull(groups, parseGroup(groupElement.getAsString()));
		}
		builder.groups(groups);
	}

	private GroupInfo parseGroup(String groupString) {
		ColumnInfo columnInfo = parseColumn(groupString);
		if (columnInfo == null) {
			return null;
		}
		return GroupInfo.create(columnInfo);
	}

	private void parseOrder() {
		if (!validAttribute(RequestKeyword.ORDER.lowerName())) {
			return;
		}
		List<OrderInfo> orders = new ArrayList<>();
		JsonElement ordersElement = jsonData.get(RequestKeyword.ORDER.lowerName());
		if (ordersElement.isJsonObject()) {
			throw new JsonDataException("order node cannot be an object");
		} else if (ordersElement.isJsonArray()) {
			((JsonArray) ordersElement).forEach(e -> addNotNull(orders, parseOrder(e.getAsString())));
		} else {
			addNotNull(orders, parseOrder(ordersElement.getAsString()));
		}
		builder.orders(orders);
	}

	private OrderInfo parseOrder(String orderString) {
		if (Strings.isNullOrEmpty(orderString)) {
			return null;
		}
		orderString = orderString.trim();
		String columnString = null;
		Order order = null;
		String[] columnOrder = orderString.split("\\s+");
		String orderTypeString = columnOrder[columnOrder.length - 1];
		if (orderTypeString.equalsIgnoreCase("asc") || orderTypeString.equalsIgnoreCase("desc")) {
			columnString = columnOrder[0];
			order = orderTypeString.equalsIgnoreCase("asc") ? Order.ASC : Order.DESC;
		} else if (orderString.startsWith("+") || orderString.startsWith("-")) {
			String symbol = orderString.substring(0, 1);
			columnString = orderString.substring(1);
			order = symbol.equals("-") ? Order.DESC : Order.ASC;
		} else {
			columnString = orderString;
			order = Order.ASC;
		}
		ColumnInfo columnInfo = parseColumn(columnString);
		if (columnInfo == null) {
			return null;
		}
		return OrderInfo.create(order, columnInfo);
	}

	private void parseLimit() {
		if (!validAttribute(RequestKeyword.LIMIT.lowerName())) {
			return;
		}
		JsonElement limitElement = jsonData.get(RequestKeyword.LIMIT.lowerName());
		if (!limitElement.isJsonArray()) {
			throw new JsonDataException("limit node must be an array");
		}
		JsonArray limitArray = (JsonArray) limitElement;
		long start = limitArray.get(0).getAsLong();
		long end = limitArray.get(1).getAsLong();
		LimitInfo limit = LimitInfo.create(start, end);
		builder.limit(limit);
	}

	private void parseFile() {
		if (!validAttribute(RequestKeyword.FILE.lowerName())) {
			return;
		}
		String file = jsonData.get(RequestKeyword.FILE.lowerName()).getAsString();
		builder.file(FileInfo.create(file));
	}

	private ValuesInfo parseValues(JsonObject valuesObject) {
		Map<String, Object> values = new LinkedHashMap<>();
		for (String column : valuesObject.keySet()) {
			JsonElement valueElement = valuesObject.get(column);
			Object value = null;
			if (valueElement.isJsonNull()) {
				//
			} else if (valueElement.isJsonPrimitive()) {
				value = valueElement.getAsString();
			} else if (valueElement.isJsonArray()) {
				value = GSON.fromJson(valueElement.toString(), Object[].class);
			} else {
				value = valueElement.toString();
			}
			values.put(column, value);
		}
		return ValuesInfo.create(values);
	}

	private void parseInsert() {
		parsePrimaryTable();
		parseInsertValues();
	}

	private void parseInsertValues() {
		if (!validAttribute(RequestKeyword.VALUES.lowerName())) {
			return;
		}
		List<ValuesInfo> insertValues = new ArrayList<>();
		JsonElement valuesElement = jsonData.get(RequestKeyword.VALUES.lowerName());
		if (valuesElement.isJsonArray()) {
			throw new UnsupportedOperationException("Unrealized.");
		} else if (valuesElement.isJsonObject()) {
			ValuesInfo valuesInfo = parseValues((JsonObject) valuesElement);
			insertValues.add(valuesInfo);
		}
		builder.insertValues(insertValues);
	}

	private void parseUpdate() {
		parsePrimaryTable();
		parseUpdateValues();
		parseWhere();
	}

	private void parseUpdateValues() {
		if (!validAttribute(RequestKeyword.VALUES.lowerName())) {
			return;
		}
		JsonElement valuesElement = jsonData.get(RequestKeyword.VALUES.lowerName());
		if (valuesElement.isJsonArray()) {
			throw new UnsupportedOperationException("Unrealized.");
		} else if (valuesElement.isJsonObject()) {
			ValuesInfo valuesInfo = parseValues((JsonObject) valuesElement);
			builder.updateValues(valuesInfo);
		}
	}

	private void parseDelete() {
		parsePrimaryTable();
		parseWhere();
	}

	private void parseSql() {
		String sql = jsonData.get(operationAttribute).getAsString();
		builder.sql(SqlInfo.create(sql));
	}

	private void parseNative() {
		String content = jsonData.get(operationAttribute).toString();
		NativeInfo nativeInfo = NativeInfo.create(content);
		builder.nativeInfo(nativeInfo);
	}

	private void parseTransaction() {
		JsonElement transactionElement = jsonData.get(operationAttribute);
		if (!transactionElement.isJsonArray()) {
			throw new JsonDataException("Transaction node must be an array");
		}
		List<SimpleInfo> simples = new ArrayList<>();
		((JsonArray) transactionElement).forEach(e -> simples.add(parseSimple(e)));
		builder.simples(simples);
	}

	private SimpleInfo parseSimple(JsonElement simpleElement) {
		if (!simpleElement.isJsonObject()) {
			throw new JsonDataException("Json must be an object");
		}
		return parse(simpleElement.getAsJsonObject());

	}

	private boolean validAttribute(String attribute) {
		if (jsonData.has(attribute) && !jsonData.get(attribute).isJsonNull()) {
			return true;
		}
		return false;
	}

	private <T> void addNotNull(List<T> list, T value) {
		if (value != null) {
			list.add(value);
		}
	}

	public static SimpleInfo parse(String json) {
		return new SimpleParser(json).parse();
	}

	public static SimpleInfo parse(JsonObject jsonData) {
		return new SimpleParser(jsonData).parse();
	}

}

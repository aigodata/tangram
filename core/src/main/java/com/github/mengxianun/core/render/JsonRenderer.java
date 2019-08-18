package com.github.mengxianun.core.render;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.JoinTableItem;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.Relationship;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonRenderer extends AbstractRenderer<JsonElement> {

	public JsonRenderer(Action action) {
		super(action);
	}

	@Override
	public JsonArray render(List<Row> rows) {
		// 主表唯一记录对象, key 为主表所有列的的值拼接的字符串, value 为主表唯一记录的对象
		JsonObject uniqueRecords = new JsonObject();
		for (Row row : rows) {
			// 主表的唯一记录对象
			JsonObject uniqueRecord;
			// 主表的唯一记录标识
			String uniqueRecordKey = createMainTableUniqueRecordKey(row);
			if (uniqueRecords.has(uniqueRecordKey)) {
				uniqueRecord = uniqueRecords.getAsJsonObject(uniqueRecordKey);
			} else {
				uniqueRecord = new JsonObject();
				uniqueRecords.add(uniqueRecordKey, uniqueRecord);
			}
			// 处理单条记录
			render(uniqueRecord, row);
		}
		JsonArray renderData = new JsonArray();
		for (String uniqueKey : uniqueRecords.keySet()) {
			renderData.add(uniqueRecords.getAsJsonObject(uniqueKey));
		}
		return renderData;
	}

	private JsonObject render(JsonObject uniqueRecord, Row row) {
		List<JoinItem> joinItems = action.getJoinItems();
		// 构建关联信息
		if (!joinItems.isEmpty() && action.getGroupItems().isEmpty()) {
			// 出现过的 join 表的对象, 用于 join 表的列再次获取已经创建的 join 表对象
			JsonObject existJoinTables = new JsonObject();
			List<ColumnItem> columnItems = action.getColumnItems();
			// 当前循环列的表数据对象
			JsonObject currentTableObject;
			for (int x = 0; x < columnItems.size(); x++) {
				ColumnItem columnItem = columnItems.get(x);
				currentTableObject = uniqueRecord;
				Object value = row.getValue(x);
				if (columnItem instanceof JoinColumnItem) {
					JoinColumnItem joinColumnItem = (JoinColumnItem) columnItem;
					JoinTableItem joinTableItem = (JoinTableItem) joinColumnItem.getTableItem();

					Table joinTable = joinTableItem.getTable();

					String existJoinTableKey = joinTableItem.getRelationships().stream()
							.map(e -> e.getPrimaryColumn().getName()).collect(Collectors.joining());
					existJoinTableKey += getTableKey(joinTable);

					if (existJoinTables.has(existJoinTableKey)) {
						JsonObject joinTableObject = existJoinTables.getAsJsonObject(existJoinTableKey);
						addColumnValue(joinTableObject, columnItem, value);
						continue;
					}

					currentTableObject = createJoinStructure(currentTableObject, joinTableItem);
					// 记录出现过的 join 表
					existJoinTables.add(existJoinTableKey, currentTableObject);

					addColumnValue(currentTableObject, columnItem, value);
				} else {
					addColumnValue(currentTableObject, columnItem, value);
				}
			}
		} else {
			List<ColumnItem> columnItems = action.getColumnItems();
			for (int i = 0; i < columnItems.size(); i++) {
				ColumnItem columnItem = columnItems.get(i);
				addColumnValue(uniqueRecord, columnItem, row.getValue(i));
			}
		}
		return uniqueRecord;
	}

	/**
	 * 生成主表每条记录的唯一标识
	 * 
	 * @param record
	 * @param action
	 * @return
	 */
	private String createMainTableUniqueRecordKey(Row row) {
		// 单表查询的情况, 每条记录为一条唯一的记录, 所以这里生成了一个唯一ID用于标识每条记录, 以保证唯一
		if (!action.isJoin()) {
			return UUID.randomUUID().toString();
		}
		StringBuilder uniqueKey = new StringBuilder();
		List<ColumnItem> columnItems = action.getColumnItems();
		for (int i = 0; i < columnItems.size(); i++) {
			ColumnItem columnItem = columnItems.get(i);
			if (!(columnItem instanceof JoinColumnItem)) { // 主表列
				Object value = row.getValue(i);
				if (value != null) {
					uniqueKey.append(value.toString());
				}
			}

		}
		return uniqueKey.toString();
	}

	private String getTableKey(Table table) {
		return App.Context.getTableKey(table);
	}

	private String getColumnKey(ColumnItem columnItem) {
		String columnKey = "";
		Column column = columnItem.getColumn();
		if (action.columnAliasEnabled() && columnItem.isCustomAlias()) { // 自定义别名
			columnKey = columnItem.getAlias();
		} else if (column == null) { // 表达式, 如函数
			columnKey = columnItem.getExpression();
		} else {
			columnKey = App.Context.getColumnAlias(column);
		}
		return columnKey;
	}

	private void addColumnValue(JsonObject record, ColumnItem columnItem, Object value) {
		String columnKey = getColumnKey(columnItem);
		addColumnValue(record, columnItem.getColumn(), columnKey, value);
	}

	private void addColumnValue(JsonObject record, Column column, String key, Object value) {
		if (column != null) {
			JsonObject config = column.getConfig();
			if (config.has(TableConfig.COLUMN_IGNORE) && config.get(TableConfig.COLUMN_IGNORE).getAsBoolean()) { // 列忽略
				return;
			}
		}
		if (value == null) {
			record.addProperty(key, (String) null);
			return;
		}

		if (column == null) {
			if (value instanceof Number) {
				record.addProperty(key, (Number) value);
			} else if (value instanceof Boolean) {
				record.addProperty(key, (Boolean) value);
			} else {
				record.addProperty(key, value.toString());
			}
		} else {
			ColumnType columnType = column.getType();
			if (columnType.isNumber()) {
				Number number = null;
				Double doubleValue = new Double(value.toString());
				if (columnType.isInteger()) {
					number = doubleValue.intValue();
				} else if (columnType.isLong()) {
					number = doubleValue.longValue();
				} else if (columnType.isDouble()) {
					// It's already a double
				}
				record.addProperty(key, render(column, number));
			} else if (columnType.isBoolean()) {
				record.addProperty(key, render(column, Boolean.parseBoolean(value.toString())));
			} else if (columnType.isLiteral()) {
				record.addProperty(key, render(column, value.toString()));
			} else if (columnType.isJson() || columnType.isArray()) {
				record.add(key, new Gson().toJsonTree(value));
			} else {
				record.addProperty(key, render(column, value.toString()));
			}
		}

	}

	private Number render(Column column, Number value) {
		if (column == null) {
			return value;
		}
		return value;
	}

	private Boolean render(Column column, Boolean value) {
		if (column == null) {
			return value;
		}
		return value;
	}

	private String render(Column column, String value) {
		if (column == null) {
			return value;
		}
		return value;
	}

	private JsonObject createJoinStructure(JsonObject currentTableObject, JoinTableItem joinTableItem) {

		Set<Relationship> relationships = joinTableItem.getRelationships();

		for (Relationship relationship : relationships) {
			Column primaryColumn = relationship.getPrimaryColumn();
			Column foreignColumn = relationship.getForeignColumn();
			AssociationType associationType = relationship.getAssociationType();
			Table foreignTable = foreignColumn.getTable();
			// 如果该关联表不是请求中指定的关联表, 不构建关系结构
			if (action.getJoinTables().contains(foreignTable)) {
				// 关联表节点名称, 主表关联字段名称_关联表名称(或别名, 以别名为主)
				String foreignTableKey = primaryColumn.getName() + "_" + getTableKey(foreignTable);
				if (currentTableObject.has(foreignTableKey)) {
					JsonElement parentElement = currentTableObject.get(foreignTableKey);
					if (parentElement.isJsonArray()) {
						JsonArray parentArray = parentElement.getAsJsonArray();
						currentTableObject = new JsonObject();
						parentArray.add(currentTableObject);
					} else {
						currentTableObject = parentElement.getAsJsonObject();
					}
				} else {
					currentTableObject = createJoinStructure(currentTableObject, foreignTableKey, associationType);
				}
			}
		}
		return currentTableObject;
	}

	private JsonObject createJoinStructure(JsonObject currentTableObject, String tableKey,
			AssociationType associationType) {
		switch (associationType) {
		case ONE_TO_ONE:
		case MANY_TO_ONE:
			currentTableObject = createJoinObject(currentTableObject, tableKey);
			break;
		case ONE_TO_MANY:
		case MANY_TO_MANY:
			currentTableObject = createJoinArray(currentTableObject, tableKey);
			break;

		default:
			break;
		}
		return currentTableObject;
	}

	private JsonObject createJoinObject(JsonObject currentTableObject, String tableKey) {
		if (currentTableObject.has(tableKey)) {
			currentTableObject = currentTableObject.getAsJsonObject(tableKey);
		} else {
			JsonObject tempJsonObject = new JsonObject();
			currentTableObject.add(tableKey, tempJsonObject);
			currentTableObject = tempJsonObject;
		}
		return currentTableObject;
	}

	private JsonObject createJoinArray(JsonObject currentTableObject, String tableKey) {
		if (currentTableObject.has(tableKey)) {
			JsonArray tempJsonArray = currentTableObject.getAsJsonArray(tableKey);
			JsonObject tempJsonObject = new JsonObject();
			tempJsonArray.add(tempJsonObject);
			currentTableObject = tempJsonObject;
		} else {
			JsonArray tempJsonArray = new JsonArray();
			JsonObject tempJsonObject = new JsonObject();
			tempJsonArray.add(tempJsonObject);
			currentTableObject.add(tableKey, tempJsonArray);
			currentTableObject = tempJsonObject;
		}
		return currentTableObject;
	}

}

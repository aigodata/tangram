package com.github.mengxianun.core.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinTableItem;
import com.github.mengxianun.core.item.RelationshipItem;
import com.github.mengxianun.core.item.TableItem;
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
		List<ColumnItem> columnItems = action.getColumnItems();

		// 每个表的数据
		Map<TableItem, JsonObject> existTableItems = new LinkedHashMap<>();
		// 获取每个表的数据
		for (int x = 0; x < columnItems.size(); x++) {
			ColumnItem columnItem = columnItems.get(x);
			TableItem tableItem = columnItem.getTableItem();
			JsonObject tableObject;
			if (existTableItems.containsKey(tableItem)) {
				tableObject = existTableItems.get(tableItem);
			} else {
				tableObject = new JsonObject();
				existTableItems.put(tableItem, tableObject);
			}
			Object value = row.getValue(x);
			addColumnValue(tableObject, columnItem, value);
		}
		// 循环所有表, 构建数据结构
		for (Entry<TableItem, JsonObject> entry : existTableItems.entrySet()) {
			JsonObject currentTableObject = uniqueRecord;
			TableItem tableItem = entry.getKey();
			JsonObject tableObject = entry.getValue();
			if (tableItem instanceof JoinTableItem) {
				JoinTableItem joinTableItem = (JoinTableItem) tableItem;
				List<RelationshipItem> relationshipItems = joinTableItem.getRelationshipItems();
				for (RelationshipItem relationshipItem : relationshipItems) {
					TableItem rightTableItem = relationshipItem.getRightTableItem();
					Relationship relationship = relationshipItem.getRelationship();
					Column primaryColumn = relationship.getPrimaryColumn();
					Column foreignColumn = relationship.getForeignColumn();
					AssociationType associationType = relationship.getAssociationType();
					Table foreignTable = foreignColumn.getTable();
					// 如果该关联表不是请求中指定的关联表, 不构建关系结构
					// 只构建请求中指定的关联表
					if (action.getJoinTables().contains(foreignTable)) {
						// 关联表节点名称, 主表关联字段名称_关联表名称(或别名, 以别名为主)
						String foreignTableKey = primaryColumn.getName() + "_" + getTableKey(foreignTable);
						if (currentTableObject.has(foreignTableKey)) {
							JsonElement parentElement = currentTableObject.get(foreignTableKey);
							if (parentElement.isJsonArray()) {
								JsonArray parentArray = parentElement.getAsJsonArray();
								// 去重
								if (!parentArray.contains(tableObject)) {
									parentArray.add(tableObject);
								}
							} else {
								currentTableObject = parentElement.getAsJsonObject();
							}
						} else {
							// 如果主表关联字段值为null, 说明主表该列的值没有关联的外表数据
							if (currentTableObject.get(primaryColumn.getName()).isJsonNull()) {
								break;
							}
							JsonObject joinTableObject = existTableItems.get(rightTableItem);
							if (associationType == AssociationType.ONE_TO_ONE
									|| associationType == AssociationType.MANY_TO_ONE) {
								currentTableObject.add(foreignTableKey, joinTableObject);
							} else {
								JsonArray joinTableArray = new JsonArray();
								joinTableArray.add(joinTableObject);
								currentTableObject.add(foreignTableKey, joinTableArray);
							}
							currentTableObject = joinTableObject;
						}
					}
				}
			} else {
				for (Entry<String, JsonElement> tableEntry : tableObject.entrySet()) {
					String columnName = tableEntry.getKey();
					JsonElement columnValue = tableEntry.getValue();
					if (!uniqueRecord.has(columnName)) {
						uniqueRecord.add(columnName, columnValue);
					}
				}
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

}

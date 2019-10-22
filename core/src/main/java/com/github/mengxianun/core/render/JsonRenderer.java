package com.github.mengxianun.core.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.GlobalConfig;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
		Map<TableItem, JsonObject> tableItemValues = parseTableItemValues(row);
		// 循环所有表, 构建数据结构
		for (Entry<TableItem, JsonObject> entry : tableItemValues.entrySet()) {
			TableItem tableItem = entry.getKey();
			JsonObject tableObject = entry.getValue();
			if (tableItem instanceof JoinTableItem) {
				if (isNull(tableObject)) {
					continue;
				}
				buildJoinTableValues(uniqueRecord, tableItemValues, (JoinTableItem) tableItem);
			} else {
				buildMainTableValues(uniqueRecord, tableObject);
			}
		}
		return uniqueRecord;
	}

	/**
	 * 解析每个 TableItem 的值, 转换为 Json 对象
	 * 
	 * @param row
	 * @return
	 */
	private Map<TableItem, JsonObject> parseTableItemValues(Row row) {
		List<ColumnItem> columnItems = row.getHeader().getColumnItems();
		Map<TableItem, JsonObject> tableItemValues = new LinkedHashMap<>();
		// 获取每个表的数据
		for (int x = 0; x < columnItems.size(); x++) {
			ColumnItem columnItem = columnItems.get(x);
			TableItem tableItem = columnItem.getTableItem();
			JsonObject tableObject;
			if (tableItemValues.containsKey(tableItem)) {
				tableObject = tableItemValues.get(tableItem);
			} else {
				tableObject = new JsonObject();
				tableItemValues.put(tableItem, tableObject);
			}
			Object value = row.getValue(x);
			addColumnValue(tableObject, columnItem, value);
		}
		return tableItemValues;
	}

	private void buildMainTableValues(JsonObject uniqueRecord, JsonObject tableObject) {
		for (Entry<String, JsonElement> tableEntry : tableObject.entrySet()) {
			String columnName = tableEntry.getKey();
			JsonElement columnValue = tableEntry.getValue();
			if (!uniqueRecord.has(columnName)) {
				uniqueRecord.add(columnName, columnValue);
			}
		}
	}

	private void buildJoinTableValues(JsonObject currentTableObject, Map<TableItem, JsonObject> tableItemValues,
			JoinTableItem joinTableItem) {
		// 关联节点连接符
		String associationConnector = App.Config.getString(GlobalConfig.ASSOCIATION_CONNECTOR);
		// 父级关联表字段, 在请求非直接关联表的查询的时候用到
		// 如存在关联关系 A-B-C, 请求 "select":"A", "join":["C"]
		Column topColumn = null;
		List<RelationshipItem> relationshipItems = joinTableItem.getRelationshipItems();
		for (int i = 0; i < relationshipItems.size(); i++) {
			RelationshipItem relationshipItem = relationshipItems.get(i);
			TableItem rightTableItem = relationshipItem.getRightTableItem();
			Relationship relationship = relationshipItem.getRelationship();
			Column primaryColumn = relationship.getPrimaryColumn();
			Column foreignColumn = relationship.getForeignColumn();
			AssociationType associationType = relationship.getAssociationType();
			Table foreignTable = foreignColumn.getTable();

			JsonObject tableObject = tableItemValues.get(rightTableItem);

			String primaryColumnAlias = primaryColumn.getAliasOrName();
			// 如果该关联表不是请求中指定的关联表, 不构建关系结构
			// 只构建请求中指定的关联表
			if (action.isJoinTable(foreignTable)) {
				// 关联表节点名称, 主表字段__关联表字段(或别名, 以别名为主)
				String foreignTableKey = primaryColumnAlias + associationConnector + foreignTable.getAliasOrName();
				if (currentTableObject.has(foreignTableKey)) { // 已经构建了关联表结构
					JsonElement parentElement = currentTableObject.get(foreignTableKey);
					if (parentElement.isJsonArray()) {
						JsonArray parentArray = parentElement.getAsJsonArray();
						if (i == relationshipItems.size() - 1) { // 最后
							// 去重
							if (!parentArray.contains(tableObject)) {
								parentArray.add(tableObject);
							}
						} else {
							currentTableObject = parentArray.get(parentArray.size() - 1).getAsJsonObject();
						}
					} else {
						currentTableObject = parentElement.getAsJsonObject();
					}
				} else { // 还未构建关联表的结构
					if (currentTableObject.has(primaryColumnAlias)) { // 主表中包含关联表的列
						if (currentTableObject.get(primaryColumnAlias).isJsonNull()) {
							// 如果主表关联字段值为null, 说明主表该列的值没有关联的外表数据
							break;
						} else { // 构建关联表的结构
							buildJoinTableObject(currentTableObject, associationType, foreignTableKey, tableObject);
							currentTableObject = tableObject;
						}
					} else { // 主表中不包含关联表的列. 可能的情况: 1. 查询fields未指定主表中的关联列   2. 非直接关联的查询, 即A-B-C, 查询A, join C的情况
						if (topColumn == null) { // 查询fields未指定主表中的关联列
							topColumn = primaryColumn;
						}
						// 有上级关联列, 即A-B-C, 查询A, join C的情况, 存在A列的情况, 即A-B_ID
						primaryColumnAlias = topColumn.getAliasOrName();
						foreignTableKey = primaryColumnAlias + associationConnector + foreignTable.getAliasOrName();
						AssociationType indirectAssociationType = App.Context.getAssociationType(topColumn.getTable(),
								foreignTable);
						buildJoinTableObject(currentTableObject, indirectAssociationType, foreignTableKey, tableObject);
						currentTableObject = tableObject;
					}
				}
				topColumn = null;
			} else {
				topColumn = primaryColumn;
			}
		}
	}

	private void buildJoinTableObject(JsonObject currentTableObject, AssociationType associationType,
			String foreignTableKey, JsonObject tableObject) {
		if (associationType == AssociationType.ONE_TO_ONE || associationType == AssociationType.MANY_TO_ONE) {
			currentTableObject.add(foreignTableKey, tableObject);
		} else {
			if (currentTableObject.has(foreignTableKey)) {
				JsonArray joinTableArray = currentTableObject.getAsJsonArray(foreignTableKey);
				joinTableArray.add(tableObject);
			} else {
				JsonArray joinTableArray = new JsonArray();
				joinTableArray.add(tableObject);
				currentTableObject.add(foreignTableKey, joinTableArray);
			}
		}
	}

	private boolean isNull(JsonElement e) {
		if (e == null || e.isJsonNull()) {
			return true;
		} else if (e.isJsonArray()) {
			JsonArray jsonArray = e.getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				if (!isNull(jsonArray.get(i))) {
					return false;
				}
			}
			return true;
		} else if (e.isJsonObject()) {
			JsonObject jsonObject = e.getAsJsonObject();
			for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				if (!isNull(entry.getValue())) {
					return false;
				}
			}
			return true;
		}
		return false;
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
		List<ColumnItem> columnItems = row.getHeader().getColumnItems();
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
				if (value instanceof JsonPrimitive) {
					number = ((JsonPrimitive) value).getAsNumber();
				} else {
					number = (Number) value;
				}
				record.addProperty(key, render(column, number));
			} else if (columnType.isBoolean()) {
				record.addProperty(key, render(column, Boolean.parseBoolean(value.toString())));
			} else if (columnType.isLiteral()) {
				record.addProperty(key, render(column, value.toString()));
			} else if (columnType.isJson() || columnType.isArray()) {
				record.add(key, App.gson().toJsonTree(value));
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

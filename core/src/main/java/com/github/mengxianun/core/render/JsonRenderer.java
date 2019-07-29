package com.github.mengxianun.core.render;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.attributes.AssociationType;
import com.github.mengxianun.core.attributes.ColumnConfigAttributes;
import com.github.mengxianun.core.attributes.TableConfigAttributes;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonRenderer extends AbstractRenderer<JsonElement> {

	public JsonRenderer(Action action) {
		super(action);
	}

	@Override
	public JsonElement render(List<Row> rows) {
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
					Table joinTable = joinColumnItem.getTableItem().getTable();
					if (existJoinTables.has(joinTable.getName())) {
						JsonObject joinTableObject = existJoinTables.getAsJsonObject(joinTable.getName());
						addColumnValue(joinTableObject, columnItem, value);
						continue;
					}
					/*
					 * 待优化
					 */
					Table mainTable = action.getTableItems().get(0).getTable();
					// join 表的直接父级表
					Table directParentTable = mainTable;
					Set<Relationship> relationships = App.Context.getRelationships(mainTable, joinTable);
					// 构建join表上层表关系
					List<Table> parentTables = relationships.stream().map(e -> e.getPrimaryColumn().getTable())
							.collect(Collectors.toList());
					// -- 构建 join 表上层结构
					for (int i = 0; i < parentTables.size() - 1; i++) {
						// 父级表, 第一个元素是主表, 跳过
						Table parentTable = parentTables.get(i + 1);
						// 如果该关联表不是请求中指定的关联表, 不构建关系结构
						if (!action.getJoinTables().contains(parentTable)) {
							continue;
						} else {
							directParentTable = parentTable;
						}
						// 已经构建了该 join 表的结构, 直接获取
						if (uniqueRecord.has(parentTable.getName())) {
							JsonElement parentElement = uniqueRecord.get(parentTable.getName());
							if (parentElement.isJsonArray()) {
								JsonArray parentArray = parentElement.getAsJsonArray();
								// 获取数组关联表的最新的元素, 即当前正在循环的元素
								currentTableObject = parentArray.get(parentArray.size() - 1).getAsJsonObject();
							} else {
								currentTableObject = parentElement.getAsJsonObject();
							}
						} else {
							AssociationType associationType = App.Context.getAssociationType(parentTables.get(i),
									parentTables.get(i + 1));
							currentTableObject = createJoinStructure(currentTableObject, parentTables.get(i + 1),
									associationType);
						}
					}
					// -- 构建 join 表结构

					AssociationType associationType = App.Context.getAssociationType(directParentTable, joinTable);
					currentTableObject = createJoinStructure(currentTableObject, joinTable, associationType);
					// 记录出现过的 join 表
					existJoinTables.add(joinTable.getName(), currentTableObject);

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

	private void addColumnValue(JsonObject record, ColumnItem columnItem, Object value) {
		// 返回 key(列) 分3种情况
		// 1. 指定了列别名的情况下, key 为指定的列别名. 例: column as alias
		// 2. 只指定了列的情况下的情况下, key 为自动列名. 例: column
		// 3. 列为表达式, 非具体字段, key 为自动生成的别名. 例: count(*)

		Column column = columnItem.getColumn();
		// 列名, 在列存在的情况下, 以列名表示, 否则按请求中列的原始内容表示
		String columnName = column == null ? columnItem.getExpression() : column.getName();
		// 如果请求中指定了列别名, 则返回结果的 key 为指定的列别名, 否则 key 为列名
		String jsonKey = action.columnAliasEnabled() && columnItem.isCustomAlias() ? columnItem.getAlias()
				: treatColumn(columnName);
		// 配置了 JSON_KEY 的情况
		if (column != null && column.getConfig().has(ColumnConfigAttributes.JSON_KEY)) {
			if (!columnItem.isCustomAlias()) {
				jsonKey = column.getConfig().get(ColumnConfigAttributes.JSON_KEY).getAsString();
			}
		}
		addColumnValue(record, column, jsonKey, value);
	}

	private void addColumnValue(JsonObject record, Column column, String key, Object value) {
		if (column != null) {
			JsonObject config = column.getConfig();
			if (config.has(TableConfigAttributes.COLUMN_IGNORE)
					&& config.get(TableConfigAttributes.COLUMN_IGNORE).getAsBoolean()) { // 列忽略
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
				if (columnType.isInteger()) {
					number = Long.parseLong(value.toString());
				} else if (columnType.isDouble()) {
					number = Double.parseDouble(value.toString());
				}
				record.addProperty(key, render(column, number));
			} else if (columnType.isBoolean()) {
				record.addProperty(key, render(column, Boolean.parseBoolean(value.toString())));
			} else if (columnType.isLiteral()) {
				record.addProperty(key, render(column, value.toString()));
			} else if (columnType.isJson()) {
				record.add(key, new JsonParser().parse(value.toString()));
			} else {
				record.addProperty(key, render(column, value.toString()));
			}
		}

	}

	/**
	 * 处理返回的列名, 变成小写
	 * 
	 * @param columnName
	 * @return
	 */
	private String treatColumn(String columnName) {
		return columnName.toLowerCase();
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

	private JsonObject createJoinStructure(JsonObject currentTableObject, Table joinTable,
			AssociationType associationType) {
		// 配置了 JSON_KEY 的情况
		String keyName = joinTable.getName();
		// 配置了 JSON_KEY 的情况
		JsonObject tableConfig = joinTable.getConfig();
		if (tableConfig.has(TableConfigAttributes.JSON_KEY)) {
			keyName = tableConfig.get(TableConfigAttributes.JSON_KEY).getAsString();
		}
		return createJoinStructure(currentTableObject, keyName, associationType);
	}

	private JsonObject createJoinStructure(JsonObject currentTableObject, String tableName,
			AssociationType associationType) {
		switch (associationType) {
		case ONE_TO_ONE:
		case MANY_TO_ONE:
			currentTableObject = createJoinObject(currentTableObject, tableName);
			break;
		case ONE_TO_MANY:
		case MANY_TO_MANY:
			currentTableObject = createJoinArray(currentTableObject, tableName);
			break;

		default:
			break;
		}
		return currentTableObject;
	}

	private JsonObject createJoinObject(JsonObject currentTableObject, String tableName) {
		if (currentTableObject.has(tableName)) {
			currentTableObject = currentTableObject.getAsJsonObject(tableName);
		} else {
			JsonObject tempJsonObject = new JsonObject();
			currentTableObject.add(tableName, tempJsonObject);
			currentTableObject = tempJsonObject;
		}
		return currentTableObject;
	}

	private JsonObject createJoinArray(JsonObject currentTableObject, String tableName) {
		if (currentTableObject.has(tableName)) {
			JsonArray tempJsonArray = currentTableObject.getAsJsonArray(tableName);
			JsonObject tempJsonObject = new JsonObject();
			tempJsonArray.add(tempJsonObject);
			currentTableObject = tempJsonObject;
		} else {
			JsonArray tempJsonArray = new JsonArray();
			JsonObject tempJsonObject = new JsonObject();
			tempJsonArray.add(tempJsonObject);
			currentTableObject.add(tableName, tempJsonArray);
			currentTableObject = tempJsonObject;
		}
		return currentTableObject;
	}

}

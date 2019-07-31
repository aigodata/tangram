package com.github.mengxianun.core;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.JoinColumnItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * 结果数据渲染器
 * 
 * @author mengxiangyun
 *
 */
@Deprecated
public class DataRenderer {

	/**
	 * 根据数据类型渲染数据.
	 * 
	 * @param data
	 * @param action
	 * @return 渲染结果数据
	 */
	public JsonElement render(JsonElement data, Action action) {
		if (data.isJsonArray()) {
			return render(data.getAsJsonArray(), action);
		} else if (data.isJsonObject()) {
			return render(data.getAsJsonObject(), action);
		} else {
			return data;
		}
	}

	private JsonElement render(JsonArray data, Action action) {
		// 主表唯一记录对象, key 为主表所有列的的值拼接的字符串, value 为主表唯一记录的对象
		JsonObject uniqueRecords = new JsonObject();
		for (JsonElement jsonElement : data) {
			// 结果数据对象
			JsonObject record = jsonElement.getAsJsonObject();
			// 主表的唯一记录对象
			JsonObject uniqueRecord;
			// 主表的唯一记录标识
			String uniqueRecordKey = createMainTableUniqueRecordKey(record, action);
			if (uniqueRecords.has(uniqueRecordKey)) {
				uniqueRecord = uniqueRecords.getAsJsonObject(uniqueRecordKey);
			} else {
				uniqueRecord = new JsonObject();
				uniqueRecords.add(uniqueRecordKey, uniqueRecord);
			}
			// 处理单条记录
			render(uniqueRecord, record, action);
		}
		JsonArray renderData = new JsonArray();
		for (String uniqueKey : uniqueRecords.keySet()) {
			renderData.add(uniqueRecords.getAsJsonObject(uniqueKey));
		}
		return renderData;

	}

	private JsonObject render(JsonObject record, Action action) {
		JsonObject result = new JsonObject();
		result = render(result, record, action);
		return result;
	}

	private JsonObject render(JsonObject uniqueRecord, JsonObject record, Action action) {
		List<JoinItem> joinItems = action.getJoinItems();
		// 构建关联信息
		if (!joinItems.isEmpty() && action.getGroupItems().isEmpty()) {
			// 出现过的 join 表的对象, 用于 join 表的列再次获取已经创建的 join 表对象
			JsonObject existJoinTables = new JsonObject();
			List<ColumnItem> columnItems = action.getColumnItems();
			// 当前循环列的表数据对象
			JsonObject currentTableObject;
			for (ColumnItem columnItem : columnItems) {
				currentTableObject = uniqueRecord;
				if (columnItem instanceof JoinColumnItem) {
					JoinColumnItem joinColumnItem = (JoinColumnItem) columnItem;
					Table joinTable = joinColumnItem.getTableItem().getTable();
					if (existJoinTables.has(joinTable.getName())) {
						JsonObject joinTableObject = existJoinTables.getAsJsonObject(joinTable.getName());
						addColumnValue(joinTableObject, columnItem, record, action);
						continue;
					}
					/*
					 * 待优化
					 */
					Table mainTable = action.getTableItems().get(0).getTable();
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
							AssociationType associationType = App.Context
									.getAssociationType(parentTables.get(i), parentTables.get(i + 1));
							currentTableObject = createJoinStructure(currentTableObject, parentTables.get(i + 1),
									associationType);
						}
					}
					// -- 构建 join 表结构
					// join 表的父级表
					Table parentTable = parentTables.get(parentTables.size() - 1);
					AssociationType associationType = App.Context.getAssociationType(parentTable,
							joinTable);
					currentTableObject = createJoinStructure(currentTableObject, joinTable, associationType);
					// 记录出现过的 join 表
					existJoinTables.add(joinTable.getName(), currentTableObject);

					addColumnValue(currentTableObject, columnItem, record, action);
				} else {
					addColumnValue(currentTableObject, columnItem, record, action);
				}
			}
		} else {
			List<ColumnItem> columnItems = action.getColumnItems();
			if (columnItems.isEmpty()) {
				return record;
			} else {
				columnItems.forEach(e -> addColumnValue(uniqueRecord, e, record, action));
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
	private String createMainTableUniqueRecordKey(JsonObject record, Action action) {
		// 单表查询的情况, 每条记录为一条唯一的记录, 所以这里生成了一个唯一ID用于标识每条记录, 以保证唯一
		if (!action.isJoin()) {
			return UUID.randomUUID().toString();
		}
		StringBuilder uniqueKey = new StringBuilder();
		List<ColumnItem> columnItems = action.getColumnItems();
		for (ColumnItem columnItem : columnItems) {
			if (!(columnItem instanceof JoinColumnItem)) { // 主表列
				Column column = columnItem.getColumn();
				// 列名
				String columnName = column == null ? columnItem.getExpression() : column.getName();
				String columnAlias = columnItem.getAlias();
				boolean hasAlias = !Strings.isNullOrEmpty(columnAlias);
				// 请求列的名称
				String columnKey = action.columnAliasEnabled() && hasAlias ? columnAlias : columnName;
				// 返回结果列的名称
				String recordKey = hasAlias ? columnAlias : columnKey;
				Object value = getValue(record, recordKey, columnName);
				if (value != null) {
					uniqueKey.append(value.toString());
				}
			}
		}
		return uniqueKey.toString();
	}

	/**
	 * 数据渲染后的 value
	 * 
	 * @param record
	 * @param columnLabel
	 * @return
	 */
	private JsonElement getValue(JsonObject record, String columnLabel, String columnName) {
		JsonElement value = null;
		if (record.has(columnLabel)) {
			value = record.get(columnLabel);
		} else if (record.has(columnLabel.toUpperCase())) {
			value = record.get(columnLabel.toUpperCase());
		} else if (record.has(columnLabel.toLowerCase())) {
			value = record.get(columnLabel.toLowerCase());
		} else if (record.has(columnName)) {
			value = record.get(columnName);
		}
		return value;
	}

	private void addColumnValue(JsonObject record, ColumnItem columnItem, JsonObject originalData, Action action) {
		Column column = columnItem.getColumn();
		// 列名, 在列存在的情况下, 以列名表示, 否则按请求中列的原始内容表示
		String columnName = column == null ? columnItem.getExpression() : column.getName();
		// 如果请求中指定了列别名, 则返回结果的 key 为指定的列别名, 否则 key 为列名
		String columnKey = action.columnAliasEnabled() && columnItem.isCustomAlias() ? columnItem.getAlias()
				: treatColumn(columnName);
		// 返回 key(列) 分3种情况
		// 1. 指定了列别名的情况下, key 为指定的列别名. 例: column as alias
		// 2. 只指定了列的情况下的情况下, key 为自动列名. 例: column
		// 3. 列为表达式, 非具体字段, key 为自动生成的别名. 例: count(*)
		String recordKey = Strings.isNullOrEmpty(columnItem.getAlias()) ? columnKey : columnItem.getAlias();
		JsonElement value = getValue(originalData, recordKey, columnName);
		addColumnValue(record, column, columnKey, value);
	}

	private void addColumnValue(JsonObject record, Column column, String columnKey, JsonElement value) {
		if (column != null) {
			JsonObject config = column.getConfig();
			if (config.has(TableConfig.COLUMN_IGNORE) && config.get(TableConfig.COLUMN_IGNORE).getAsBoolean()) { // 列忽略
				return;
			}
		}
		if (value == null || value.isJsonNull()) {
			record.addProperty(columnKey, (String) null);
		} else if (value.isJsonPrimitive()) {
			JsonPrimitive primitive = value.getAsJsonPrimitive();
			if (primitive.isNumber()) {
				Number number = value.getAsNumber();
				if (number instanceof Byte || number instanceof Short || number instanceof Integer
						|| number instanceof Long) {
					number = value.getAsLong();
				} else if (number instanceof Float || number instanceof Double) {
					number = value.getAsDouble();
				} else if (number instanceof BigDecimal) {
					// if (value.getAsBigDecimal().stripTrailingZeros().scale() <= 0) { // 整数
					// number = value.getAsBigDecimal().longValue();
					// } else {
					// number = value.getAsBigDecimal().doubleValue();
					// }
				}
				record.addProperty(columnKey, render(column, number));
			} else if (primitive.isBoolean()) {
				record.addProperty(columnKey, render(column, primitive.getAsBoolean()));
			} else if (primitive.isString()) {
				record.addProperty(columnKey, render(column, primitive.getAsString()));
			} else {
				record.addProperty(columnKey, render(column, primitive.getAsString()));
			}
		} else {
			String strVal = value.getAsJsonObject().get("value").getAsString();
			record.add(columnKey, new com.google.gson.JsonParser().parse(strVal));
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
		return createJoinStructure(currentTableObject, joinTable.getName(), associationType);
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

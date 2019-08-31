package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.config.TableConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DefaultTable implements Table {

	private String name;
	private TableType type;
	private Schema schema;
	private String remarks;
	private List<Column> columns;
	private List<Column> primaryKeys;

	private JsonObject info;
	// 自定义配置信息
	private JsonObject config = new JsonObject();

	public DefaultTable() {
		this.columns = new ArrayList<>();
		this.primaryKeys = new ArrayList<>();
	}

	public DefaultTable(String name) {
		this();
		this.name = name;
	}

	public DefaultTable(String name, TableType type) {
		this(name);
		this.type = type;

	}

	public DefaultTable(String name, TableType type, Schema schema) {
		this(name, type);
		this.schema = schema;
	}

	public DefaultTable(String name, TableType type, Schema schema, String remarks) {
		this(name, type, schema);
		this.remarks = remarks;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public TableType getType() {
		return type;
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public List<String> getColumnNames() {
		return columns.stream().map(column -> column.getName()).collect(Collectors.toList());
	}

	@Override
	public Column getColumnByName(String columnName) {
		if (columnName == null) {
			return null;
		}

		List<Column> foundColumns = new ArrayList<>();
		for (Column column : columns) {
			if (column.getName().equalsIgnoreCase(columnName)) {
				foundColumns.add(column);
			}
		}

		if (foundColumns.isEmpty()) {
			return null;
		} else if (foundColumns.size() == 1) {
			return foundColumns.get(0);
		}

		for (Column column : foundColumns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		return foundColumns.get(0);
	}

	@Override
	public List<Column> getPrimaryKeys() {
		return primaryKeys;
	}

	@Override
	public String getRemarks() {
		return remarks;
	}

	@Override
	public String getDisplayName() {
		if (config != null && config.has(TableConfig.DISPLAY)) {
			return config.get(TableConfig.DISPLAY).getAsString();
		}
		return name;
	}

	@Override
	public JsonObject getInfo() {
		if (info != null && info.size() > 0) {
			return info;
		}
		info = new JsonObject();
		info.addProperty("name", name);
		info.addProperty("type", type.name());
		info.addProperty("remarks", remarks);
		JsonArray columnsInfo = new JsonArray();
		columns.forEach(e -> columnsInfo.add(e.getInfo()));
		info.add("columns", columnsInfo);
		return info;
	}

	@Override
	public JsonObject getConfig() {
		return config;
	}

	public void addColumn(Column column) {
		boolean match = columns.parallelStream().anyMatch(e -> e.getName().equals(column.getName()));
		if (!match) {
			columns.add(column);
		}
	}

	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public void addPrimaryKey(Column column) {
		if (!primaryKeys.contains(column)) {
			primaryKeys.add(column);
		}
	}

	public void addPrimaryKey(String columnName) {
		addPrimaryKey(getColumnByName(columnName));
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public void setType(TableType type) {
		this.type = type;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public void setPrimaryKeys(List<Column> primaryKeys) {
		this.primaryKeys = primaryKeys;
	}

	public void setConfig(JsonObject config) {
		this.config = config;
	}

}

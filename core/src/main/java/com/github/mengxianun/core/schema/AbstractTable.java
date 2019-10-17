package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.config.TableConfig;
import com.google.gson.JsonObject;

public abstract class AbstractTable implements Table {

	protected final String name;
	protected final TableType type;
	protected final Schema schema;
	protected final List<Column> columns;
	protected final List<Column> primaryKeys;

	// custom config
	protected JsonObject config = new JsonObject();

	public AbstractTable(String name, TableType type, Schema schema) {
		this.name = name;
		this.type = type;
		this.schema = schema;
		this.columns = new ArrayList<>();
		this.primaryKeys = new ArrayList<>();
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
		return columns.stream().map(Column::getName).collect(Collectors.toList());
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
	public String getDisplayName() {
		if (config != null && config.has(TableConfig.DISPLAY)) {
			return config.get(TableConfig.DISPLAY).getAsString();
		}
		return name;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("name", name);
		info.put("type", type.name());

		List<Map<String, Object>> columnsInfo = new ArrayList<>();
		columns.forEach(e -> columnsInfo.add(e.getInfo()));
		info.put("columns", columnsInfo);
		return info;
	}

	@Override
	public JsonObject getConfig() {
		return config;
	}

	@Override
	public void setConfig(JsonObject config) {
		this.config = config;
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

}

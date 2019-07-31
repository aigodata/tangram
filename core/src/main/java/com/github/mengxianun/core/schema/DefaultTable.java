package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.config.ColumnConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DefaultTable implements Table {

	private String name;
	private Schema schema;
	private String remarks;
	private List<Column> columns;

	private JsonObject info;
	// 自定义配置i信息
	private JsonObject config = new JsonObject();

	public DefaultTable() {
		this.columns = new ArrayList<>();
	}

	public DefaultTable(String name) {
		this();
		this.name = name;
	}

	public DefaultTable(String name, Schema schema) {
		this();
		this.name = name;
		this.schema = schema;
	}

	public DefaultTable(String name, Schema schema, String remarks) {
		this();
		this.name = name;
		this.schema = schema;
		this.remarks = remarks;
	}

	public DefaultTable(String name, Schema schema, String remarks, List<Column> columns) {
		this.name = name;
		this.schema = schema;
		this.remarks = remarks;
		this.columns = columns;

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
			} else if (column.getConfig().has(ColumnConfig.ALIAS)) {
				String alias = column.getConfig().get(ColumnConfig.ALIAS).getAsString();
				if (alias.equals(columnName)) {
					foundColumns.add(column);
				}
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
		return null;
	}

	@Override
	public String getRemarks() {
		return remarks;
	}

	@Override
	public JsonObject getInfo() {
		if (info != null && info.size() > 0) {
			return info;
		}
		info = new JsonObject();
		info.addProperty("name", name);
		info.addProperty("remarks", remarks);
		JsonArray columnsInfo = new JsonArray();
		for (Column column : columns) {
			String columnName = column.getName();
			String columnType = column.getType().getName();

			JsonObject columnInfo = new JsonObject();
			columnInfo.addProperty("name", columnName);
			columnInfo.addProperty("type", columnType);
			columnsInfo.add(columnInfo);
		}
		info.add("columns", columnsInfo);
		return info;
	}

	@Override
	public JsonObject getConfig() {
		return config;
	}

	public void addColumn(Column column) {
		columns.add(column);
	}

	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSchema(Schema schema) {
		this.schema = schema;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public void setConfig(JsonObject config) {
		this.config = config;
	}

}

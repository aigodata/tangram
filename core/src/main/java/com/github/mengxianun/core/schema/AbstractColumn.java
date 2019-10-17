package com.github.mengxianun.core.schema;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public abstract class AbstractColumn implements Column {

	protected final String name;
	protected final ColumnType columnType;
	protected final Table table;

	// custom config
	protected JsonObject config = new JsonObject();

	public AbstractColumn(String name, ColumnType columnType, Table table) {
		this.name = name;
		this.columnType = columnType;
		this.table = table;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ColumnType getType() {
		return columnType;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public boolean isPrimaryKey() {
		return false;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("name", name);
		info.put("type", columnType.getName());
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

}

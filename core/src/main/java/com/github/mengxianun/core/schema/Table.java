package com.github.mengxianun.core.schema;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

public interface Table extends Name {

	public Schema getSchema();

	public TableType getType();

	public int getColumnCount();

	public List<Column> getColumns();

	public List<String> getColumnNames();

	public Column getColumnByName(String columnName);

	public List<Column> getPrimaryKeys();

	public String getRemarks();

	public String getDisplayName();

	public Map<String, Object> getInfo();

	public JsonObject getConfig();

	default void setConfig(JsonObject config) {}

}

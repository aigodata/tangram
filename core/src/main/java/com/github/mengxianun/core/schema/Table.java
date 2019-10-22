package com.github.mengxianun.core.schema;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

public interface Table extends Name {

	Schema getSchema();

	TableType getType();

	int getColumnCount();

	List<Column> getColumns();

	List<String> getColumnNames();

	Column getColumn(String nameOrAlias);

	Column getColumnByName(String columnName);

	List<Column> getPrimaryKeys();

	String getDisplayName();

	Map<String, Object> getInfo();

	public String getAliasOrName();

	JsonObject getConfig();

	void setConfig(JsonObject config);

}

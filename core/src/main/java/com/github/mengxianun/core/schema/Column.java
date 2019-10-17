package com.github.mengxianun.core.schema;

import java.util.Map;

import com.google.gson.JsonObject;

public interface Column extends Name {

	public ColumnType getType();

	public Table getTable();

	public boolean isPrimaryKey();

	public Map<String, Object> getInfo();

	public JsonObject getConfig();

	default void setConfig(JsonObject config) {
	}

}

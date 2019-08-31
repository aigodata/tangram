package com.github.mengxianun.core.schema;

import java.util.Map;

import com.google.gson.JsonObject;

public interface Column extends Name {

	public Table getTable();

	public ColumnType getType();

	public Boolean isNullable();

	public String getRemarks();

	public Integer getColumnSize();

	public boolean isPrimaryKey();

	public Column getRelationColumn();

	public Map<String, Object> getInfo();

	public JsonObject getConfig();

	default void setConfig(JsonObject config) {
	}

}

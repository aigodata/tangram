package com.github.mengxianun.core.schema;

import java.io.Serializable;

import com.google.gson.JsonObject;

public interface Column extends Serializable, Name {

	public Table getTable();

	public Boolean isNullable();

	public String getRemarks();

	public Integer getColumnSize();

	public boolean isPrimaryKey();

	public Column getRelationColumn();

	public JsonObject getConfig();

	default void setConfig(JsonObject config) {
	}

}

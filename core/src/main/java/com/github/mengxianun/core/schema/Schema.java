package com.github.mengxianun.core.schema;

import java.util.List;

import com.google.gson.JsonObject;

public interface Schema extends Name {

	public String getCatalog();

	public int getTableCount();

	public List<Table> getTables();

	public List<String> getTableNames();

	public Table getTableByName(String tableName);

	public JsonObject getInfo();

}

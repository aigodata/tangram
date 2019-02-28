package com.github.mengxianun.core;

import java.util.List;

import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.JsonElement;

public interface DataContext {

	public JsonElement action(Action action);

	public JsonElement action(Action... actions);

	public JsonElement executeNative(String script);

	public JsonElement executeNative(Table table, String script);

	public List<Schema> getSchemas();

	public Schema getDefaultSchema();

	public Schema getSchema(String schemaName);

	public Table getTable(String tableName);

	public Table getTable(String schemaName, String tableName);

	public Column getColumn(String tableName, String columnName);

	public Column getColumn(String schemaName, String tableName, String columnName);

	public String getIdentifierQuoteString();

	public Dialect getDialect();

	public void destroy() throws Throwable;

}

package com.github.mengxianun.core;

import java.util.List;

import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.JsonElement;

public abstract class AbstractDataContext implements DataContext {

	protected Metadata metadata = new Metadata();

	protected Dialect dialect;

	protected abstract void initializeMetadata();

	@Override
	public JsonElement executeNative(Table table, String script) {
		return executeNative(script);
	}

	@Override
	public List<Schema> getSchemas() {
		return metadata.getSchemas();
	}

	@Override
	public Schema getDefaultSchema() {
		return metadata.getDefaultSchema();
	}

	@Override
	public Schema getSchema(String schemaName) {
		return metadata.getSchema(schemaName);
	}

	@Override
	public Table getTable(String tableName) {
		return metadata.getTable(tableName);
	}

	@Override
	public Table getTable(String schemaName, String tableName) {
		return metadata.getTable(schemaName, tableName);
	}

	@Override
	public Column getColumn(String tableName, String columnName) {
		return metadata.getColumn(tableName, columnName);
	}

	@Override
	public Column getColumn(String schemaName, String tableName, String columnName) {
		return metadata.getColumn(schemaName, tableName, columnName);
	}

	@Override
	public String getIdentifierQuoteString() {
		return metadata.getIdentifierQuoteString();
	}

	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public void destroy() throws Throwable {
		// TODO
	}

}

package com.github.mengxianun.core.schema;

import com.google.gson.JsonObject;

public class DefaultColumn implements Column {

	private static final long serialVersionUID = 1L;
	private String name;
	private Table table;
	private Boolean nullable;
	private String remarks;
	private Integer columnSize;
	private Column relationColumn;
	// 自定义配置信息
	private JsonObject config = new JsonObject();

	public DefaultColumn() {
	}

	public DefaultColumn(String name) {
		this.name = name;
	}

	public DefaultColumn(String name, Table table) {
		this.name = name;
		this.table = table;
	}

	public DefaultColumn(String name, Table table, Boolean nullable, String remarks, Integer columnSize) {
		this.name = name;
		this.table = table;
		this.nullable = nullable;
		this.remarks = remarks;
		this.columnSize = columnSize;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Table getTable() {
		return table;
	}

	@Override
	public Boolean isNullable() {
		return nullable;
	}

	@Override
	public String getRemarks() {
		return remarks;
	}

	@Override
	public Integer getColumnSize() {
		return columnSize;
	}

	@Override
	public boolean isPrimaryKey() {
		return false;
	}

	@Override
	public Column getRelationColumn() {
		return relationColumn;
	}

	@Override
	public JsonObject getConfig() {
		return config;
	}

	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}

	public void setRelationColumn(Column relationColumn) {
		this.relationColumn = relationColumn;
	}

	public void setConfig(JsonObject config) {
		this.config = config;
	}

}

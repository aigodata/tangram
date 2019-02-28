package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultSchema implements Schema {

	private String name;
	private List<Table> tables;

	public DefaultSchema() {
		this.tables = new ArrayList<>();
	}

	public DefaultSchema(String name) {
		this();
		this.name = name;
	}

	public DefaultSchema(String name, List<Table> tables) {
		this.name = name;
		this.tables = tables;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getTableCount() {
		return tables.size();
	}

	@Override
	public List<Table> getTables() {
		return tables;
	}

	@Override
	public List<String> getTableNames() {
		return tables.stream().map(table -> table.getName()).collect(Collectors.toList());
	}

	@Override
	public Table getTableByName(String tableName) {
		if (tableName == null) {
			return null;
		}

		List<Table> foundTables = new ArrayList<>();
		for (Table table : tables) {
			if (table.getName().equalsIgnoreCase(tableName)) {
				foundTables.add(table);
			}
		}

		if (foundTables.isEmpty()) {
			return null;
		} else if (foundTables.size() == 1) {
			return foundTables.get(0);
		}

		for (Table table : foundTables) {
			if (table.getName().equals(tableName)) {
				return table;
			}
		}

		return foundTables.get(0);
	}

	public void addTable(Table table) {
		tables.add(table);
	}

	public void removeTable(Table table) {
		tables.remove(table);
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

}

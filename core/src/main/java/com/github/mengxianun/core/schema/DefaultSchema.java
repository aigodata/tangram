package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.config.TableConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DefaultSchema implements Schema {

	private String name;
	private String catalog;
	private List<Table> tables;

	private JsonObject info;

	public DefaultSchema() {
		this.tables = new ArrayList<>();
	}

	public DefaultSchema(String name) {
		this();
		this.name = name;
		this.catalog = name;
	}

	public DefaultSchema(String name, String catalog) {
		this(name);
		this.catalog = catalog;
	}

	public DefaultSchema(String name, String catalog, List<Table> tables) {
		this(name, catalog);
		this.tables = tables;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getCatalog() {
		return catalog;
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
			} else if (table.getConfig().has(TableConfig.ALIAS)) {
				String alias = table.getConfig().get(TableConfig.ALIAS).getAsString();
				if (alias.equals(tableName)) {
					foundTables.add(table);
				}
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

	@Override
	public JsonObject getInfo() {
		if (info != null && info.size() > 0) {
			return info;
		}
		info = new JsonObject();
		info.addProperty("name", name);
		JsonArray tablesInfo = new JsonArray();
		tables.stream().forEach(e -> tablesInfo.add(e.getInfo()));
		info.add("tables", tablesInfo);
		return info;
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

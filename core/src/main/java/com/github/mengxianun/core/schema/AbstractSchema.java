package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.App.Config;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.config.TableConfig;
import com.google.common.base.Strings;

public abstract class AbstractSchema implements Schema {

	protected final String name;
	protected final List<Table> tables;

	public AbstractSchema(String name) {
		this.name = name;
		this.tables = new ArrayList<>();
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
		return tables.stream().map(Table::getName).collect(Collectors.toList());
	}

	@Override
	public Table getTable(String nameOrAlias) {
		if (Strings.isNullOrEmpty(nameOrAlias)) {
			return null;
		}

		// 1 根据别名查询
		for (Table table : tables) {
			if (table.getConfig().has(TableConfig.ALIAS)) { // 表配置文件配置的表别名
				String alias = table.getConfig().get(TableConfig.ALIAS).getAsString();
				if (alias.equals(nameOrAlias)) {
					return table;
				}
			} else if (Config.has(GlobalConfig.TABLE_ALIAS_EXPRESSION)) { // 全局配置的表别名
				if (nameOrAlias.equalsIgnoreCase(table.getAliasOrName())) {
					return table;
				}
			}
		}

		// 2 根据实名查询
		return getTableByName(nameOrAlias);
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

	@Override
	public void addTable(Table table) {
		if (table == null) {
			return;
		}
		boolean match = tables.parallelStream().anyMatch(e -> e.getName().equals(table.getName()));
		if (!match) {
			tables.add(table);
		}
	}

	@Override
	public void removeTable(Table table) {
		tables.remove(table);
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("schema", name);

		List<Map<String, Object>> tablesInfo = new ArrayList<>();
		tables.stream().forEach(e -> tablesInfo.add(e.getInfo()));
		info.put("tables", tablesInfo);
		return info;
	}

}

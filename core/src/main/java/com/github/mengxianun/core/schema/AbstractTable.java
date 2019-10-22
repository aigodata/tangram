package com.github.mengxianun.core.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.App.Config;
import com.github.mengxianun.core.config.ColumnConfig;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.config.TableConfig;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

public abstract class AbstractTable implements Table {

	protected final String name;
	protected final TableType type;
	protected final Schema schema;
	protected final List<Column> columns;
	protected final List<Column> primaryKeys;

	// custom config
	protected JsonObject config = new JsonObject();

	public AbstractTable(String name, TableType type, Schema schema) {
		this.name = name;
		this.type = type;
		this.schema = schema;
		this.columns = new ArrayList<>();
		this.primaryKeys = new ArrayList<>();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public TableType getType() {
		return type;
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public List<Column> getColumns() {
		return columns;
	}

	@Override
	public List<String> getColumnNames() {
		return columns.stream().map(Column::getName).collect(Collectors.toList());
	}

	@Override
	public Column getColumn(String nameOrAlias) {
		if (Strings.isNullOrEmpty(nameOrAlias)) {
			return null;
		}
		// 1 根据别名查询
		for (Column column : columns) {
			if (column.getConfig().has(ColumnConfig.ALIAS)) {
				String alias = column.getConfig().get(ColumnConfig.ALIAS).getAsString();
				if (alias.equals(nameOrAlias)) {
					return column;
				}
			}
		}
		// 2 根据实名查询
		return getColumnByName(nameOrAlias);
	}

	@Override
	public Column getColumnByName(String columnName) {
		if (columnName == null) {
			return null;
		}

		List<Column> foundColumns = new ArrayList<>();
		for (Column column : columns) {
			if (column.getName().equalsIgnoreCase(columnName)) {
				foundColumns.add(column);
			}
		}

		if (foundColumns.isEmpty()) {
			return null;
		} else if (foundColumns.size() == 1) {
			return foundColumns.get(0);
		}

		for (Column column : foundColumns) {
			if (column.getName().equals(columnName)) {
				return column;
			}
		}

		return foundColumns.get(0);
	}

	@Override
	public List<Column> getPrimaryKeys() {
		return primaryKeys;
	}

	@Override
	public String getDisplayName() {
		if (config != null && config.has(TableConfig.DISPLAY)) {
			return config.get(TableConfig.DISPLAY).getAsString();
		}
		return name;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("name", name);
		info.put("type", type.name());

		List<Map<String, Object>> columnsInfo = new ArrayList<>();
		columns.forEach(e -> columnsInfo.add(e.getInfo()));
		info.put("columns", columnsInfo);
		return info;
	}

	@Override
	public String getAliasOrName() {
		if (config.has(TableConfig.ALIAS)) { // 表配置文件配置的表别名
			return config.get(TableConfig.ALIAS).getAsString();
		} else if (Config.has(GlobalConfig.TABLE_ALIAS_EXPRESSION)) { // 全局配置的表别名
			return getAliasKey(name);
		} else {
			return name;
		}
	}

	private String getAliasKey(String element) {
		JexlEngine jexl = new JexlBuilder().create();
		String jexlExp = App.Config.getString(GlobalConfig.TABLE_ALIAS_EXPRESSION);
		JexlExpression e = jexl.createExpression(jexlExp);
		JexlContext jc = new MapContext();
		jc.set("$", element);
		return e.evaluate(jc).toString();
	}

	@Override
	public JsonObject getConfig() {
		return config;
	}

	@Override
	public void setConfig(JsonObject config) {
		this.config = config;
	}

	public void addColumn(Column column) {
		boolean match = columns.parallelStream().anyMatch(e -> e.getName().equals(column.getName()));
		if (!match) {
			columns.add(column);
		}
	}

	public void addColumns(List<Column> columns) {
		columns.forEach(this::addColumn);
	}

	public void removeColumn(Column column) {
		columns.remove(column);
	}

	public void addPrimaryKey(Column column) {
		if (!primaryKeys.contains(column)) {
			primaryKeys.add(column);
		}
	}

	public void addPrimaryKey(String columnName) {
		addPrimaryKey(getColumnByName(columnName));
	}

}

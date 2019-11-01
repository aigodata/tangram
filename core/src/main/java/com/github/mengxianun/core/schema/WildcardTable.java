package com.github.mengxianun.core.schema;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.mengxianun.core.schema.TableSettings.Builder;
import com.google.gson.JsonObject;

public class WildcardTable implements Table {

	protected final String name;
	protected final Schema schema;
	protected TableSettings settings = TableSettings.defaultSettings();
	private List<Table> tables;

	public WildcardTable(String name, Schema schema, List<Table> tables) {
		this.name = name;
		this.schema = schema;
		this.tables = tables;
		settings = reCreateSettings();
	}

	private TableSettings reCreateSettings() {
		Builder builder = TableSettings.builder();
		Optional<Integer> maxQueryFieldsOptional = tables.stream().map(e -> e.getSettings().maxQueryFields())
				.max(Comparator.comparing(Integer::valueOf));
		if (maxQueryFieldsOptional.isPresent()) {
			builder.maxQueryFields(maxQueryFieldsOptional.get());
		}
		return builder.build();
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
		return TableType.OTHER;
	}

	@Override
	public int getColumnCount() {
		return (int) tables.stream().flatMap(e -> e.getColumns().stream()).count();
	}

	@Override
	public List<Column> getColumns() {
		return tables.stream().flatMap(e -> e.getColumns().stream()).collect(Collectors.toList());
	}

	@Override
	public List<String> getColumnNames() {
		return tables.stream().flatMap(e -> e.getColumnNames().stream()).collect(Collectors.toList());
	}

	@Override
	public Column getColumn(String nameOrAlias) {
		for (Table table : tables) {
			Column column = table.getColumn(nameOrAlias);
			if (column != null) {
				return column;
			}
		}
		return null;
	}

	@Override
	public Column getColumnByName(String columnName) {
		for (Table table : tables) {
			Column column = table.getColumnByName(columnName);
			if (column != null) {
				return column;
			}
		}
		return null;
	}

	@Override
	public List<Column> getPrimaryKeys() {
		return tables.stream().flatMap(e -> e.getPrimaryKeys().stream()).collect(Collectors.toList());
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = new HashMap<>();
		for (Table table : tables) {
			info.put(table.getAliasOrName(), table.getInfo());
		}
		return info;
	}

	@Override
	public String getAliasOrName() {
		return name;
	}

	@Override
	public JsonObject getConfig() {
		return new JsonObject();
	}

	@Override
	public void setConfig(JsonObject config) {
		throw new UnsupportedOperationException();
	}

	@Override
	public TableSettings getSettings() {
		return settings;
	}

	@Override
	public void setSettings(TableSettings settings) {
		this.settings = settings;
	}

}

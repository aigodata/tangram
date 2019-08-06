package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;

/**
 * @author mengxiangyun
 *
 */
/**
 * @author mengxiangyun
 *
 */
public class Metadata {

	// public name attributes
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String VERSION = "version";
	public static final String REMARKS = "remarks";

	public static final String URL = "url";
	public static final String DATA_SOURCES = "dataSources";
	public static final String SCHEMAS = "schemas";
	public static final String SCHEMA = "schema";
	public static final String DEFAULT_SCHEMA = "defaultSchema";
	public static final String TABLES = "tables";
	public static final String TABLE = "table";
	public static final String COLUMNS = "columns";
	public static final String COLUMN_DATA_TYPE = "dataType";
	public static final String COLUMN_TYPE_NAME = "typeName";
	public static final String COLUMN_SIZE = "size";
	public static final String COLUMN_NULLABLE = "nullable";
	public static final String COLUMN_IS_AUTOINCREMENT = "isAutoincrement";

	private List<Schema> schemas;

	private String defaultCatalogName;
	@Deprecated
	private String defaultSchemaName;

	public Metadata() {
		this(new ArrayList<>());
	}

	public Metadata(List<Schema> schemas) {
		this.schemas = schemas;
	}

	public Metadata(List<Schema> schemas, String defaultSchemaName) {
		this(schemas);
		this.defaultSchemaName = defaultSchemaName;
	}

	public List<Schema> getSchemas() {
		return schemas;
	}

	public List<String> getSchemaNames() {
		return schemas.stream().map(Schema::getName).collect(Collectors.toList());
	}

	public Schema getSchema(String schemaName) {
		if (Strings.isNullOrEmpty(schemaName)) {
			return schemas.get(0);
		}

		List<Schema> foundSchemas = new ArrayList<>();
		for (Schema schema : schemas) {
			if (schemaName.equalsIgnoreCase(schema.getName())) {
				foundSchemas.add(schema);
			}
		}

		if (foundSchemas.isEmpty()) {
			return null;
		} else if (foundSchemas.size() == 1) {
			return foundSchemas.get(0);
		}

		for (Schema schema : foundSchemas) {
			if (schema.getName().equals(schemaName)) {
				return schema;
			}
		}

		return foundSchemas.get(0);
	}

	public Schema getDefaultSchema() {
		return getSchema(defaultSchemaName);
	}

	public void addSchema(Schema schema) {
		this.schemas.add(schema);
	}

	public List<Table> getTables(String schemaName) {
		Schema schema = getSchema(schemaName);
		if (schema == null) {
			return Collections.emptyList();
		}
		return schema.getTables();
	}

	public Table getTable(String tableName) {
		Table table = getDefaultSchema().getTableByName(tableName);
		if (table == null) {
			for (Schema schema : schemas) {
				table = schema.getTableByName(tableName);
				if (table != null) {
					break;
				}
			}
		}
		return table;
	}

	public Table getTable(String schemaName, String tableName) {
		Schema schema = getSchema(schemaName);
		if (schema == null) {
			return null;
		}
		return schema.getTableByName(tableName);
	}

	public List<Column> getColumns(String schemaName, String tableName) {
		Table table = getTable(schemaName, tableName);
		if (table == null) {
			return Collections.emptyList();
		}
		return table.getColumns();
	}

	public Column getColumn(String tableName, String columnName) {
		Table table = getTable(tableName);
		if (table == null) {
			return null;
		}
		return table.getColumnByName(columnName);
	}

	public Column getColumn(String schemaName, String tableName, String columnName) {
		Table table = getTable(schemaName, tableName);
		if (table == null) {
			return null;
		}
		return table.getColumnByName(columnName);
	}

	public String getDefaultCatalogName() {
		return defaultCatalogName;
	}

	public void setDefaultCatalogName(String defaultCatalogName) {
		this.defaultCatalogName = defaultCatalogName;
	}

	public String getDefaultSchemaName() {
		return defaultSchemaName;
	}

	public void setDefaultSchemaName(String defaultSchemaName) {
		this.defaultSchemaName = defaultSchemaName;
	}

	public void setSchemas(List<Schema> schemas) {
		this.schemas = schemas;
	}


}

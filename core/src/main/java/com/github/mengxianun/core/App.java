package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.text.RandomStringGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.ColumnConfig;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.permission.AuthorizationInfo;
import com.github.mengxianun.core.permission.PermissionPolicy;
import com.github.mengxianun.core.permission.TablePermission;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.RelationshipPath;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

/**
 * Application center
 * 
 * @author mengxiangyun
 *
 */
public final class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);
	private static Injector injector = Guice.createInjector(Stage.PRODUCTION, new AppModule());
	private static final Gson gson = new GsonBuilder().serializeNulls().create();
	private static Configuration configuration;
	private static final Map<String, DataContext> dataContexts = new ConcurrentHashMap<>();
	// DataContext for the current thread
	private static final ThreadLocal<DataContext> currentDataContext = new ThreadLocal<>();
	// AuthorizationInfo
	private static AuthorizationInfo authorizationInfo;
	// <source, <table, TablePermissions>>
	private static final Map<String, Map<String, List<TablePermission>>> permissions = new ConcurrentHashMap<>();

	private App() {
		throw new AssertionError();
	}

	public static Map<String, DataContext> getDataContexts() {
		return dataContexts;
	}

	public static Set<String> getDataContextNames() {
		return dataContexts.keySet();
	}

	public static DataContext getDataContext(String name) {
		return dataContexts.get(name);
	}

	public static boolean hasDataContext(String name) {
		return dataContexts.containsKey(name);
	}

	public static void addDataContext(String name, DataContext dataContext) {
		dataContexts.put(name, dataContext);
		logger.info("Add new {} [{}]", dataContext.getClass().getSimpleName(), name);
	}

	public static void deleteDataContext(String name) {
		if (!hasDataContext(name)) {
			throw new DataException("Data source [%s] does not exist", name);
		}
		DataContext dataContext = getDataContext(name);
		dataContext.destroy();
		dataContexts.remove(name);
		logger.info("Remove {} [{}]", dataContext.getClass().getSimpleName(), name);
	}

	public static DataContext getDefaultDataContext() {
		return dataContexts.get(getDefaultDataSource());
	}

	public static String getDefaultDataSource() {
		return Config.getString(GlobalConfig.DEFAULT_DATASOURCE);
	}

	public static void setDefaultDataSource(String name) {
		if (hasDataContext(name)) {
			Config.set(GlobalConfig.DEFAULT_DATASOURCE, name);
		}
		throw new DataException("Data source [%s] does not exist", name);
	}

	public static void initDefaultDataSource() {
		if (Config.has(GlobalConfig.DEFAULT_DATASOURCE)) {
			String defaultDataSource = Config.getString(GlobalConfig.DEFAULT_DATASOURCE);
			if (!Strings.isNullOrEmpty(defaultDataSource)) {
				return;
			}
		}
		Set<String> dataContextNames = getDataContextNames();
		if (!dataContextNames.isEmpty()) {
			// Set the first data source as the default data source
			Config.set(GlobalConfig.DEFAULT_DATASOURCE, dataContextNames.iterator().next());
		}
	}

	public static void setCurrentDataContext(String name) {
		setCurrentDataContext(getDataContext(name));
	}

	public static void setCurrentDataContext(DataContext dataContext) {
		currentDataContext.set(dataContext);
	}

	public static DataContext currentDataContext() {
		return currentDataContext.get();
	}

	public static Configuration getConfiguration() {
		return configuration;
	}

	public static void setConfiguration(Configuration configuration) {
		App.configuration = configuration;
	}

	public static AuthorizationInfo getAuthorizationInfo() {
		return authorizationInfo;
	}

	public static void setAuthorizationInfo(AuthorizationInfo authorizationInfo) {
		App.authorizationInfo = authorizationInfo;
		List<TablePermission> tablePermissions = authorizationInfo.getTablePermissionsSupplier().get();
		if (tablePermissions != null && !tablePermissions.isEmpty()) {
			for (TablePermission tablePermission : tablePermissions) {
				String source = tablePermission.source();
				String table = tablePermission.table();
				if (Strings.isNullOrEmpty(source)) {
					source = getDefaultDataSource();
				}
				if (hasSourcePermissions(source)) {
					Map<String, List<TablePermission>> sourcePermissions = getSourcePermissions(source);
					if (sourcePermissions.containsKey(table)) {
						List<TablePermission> sourceTablePermissions = sourcePermissions.get(table);
						sourceTablePermissions.add(tablePermission);
					} else {
						List<TablePermission> sourceTablePermissions = new ArrayList<>();
						sourceTablePermissions.add(tablePermission);
						sourcePermissions.put(table, sourceTablePermissions);
					}
				} else {
					Map<String, List<TablePermission>> sourcePermissions = new HashMap<>();
					List<TablePermission> sourceTablePermissions = new ArrayList<>();
					sourceTablePermissions.add(tablePermission);
					sourcePermissions.put(table, sourceTablePermissions);
					permissions.put(source, sourcePermissions);
				}
			}
		}
	}

	public static Table getUserTable() {
		String userSource = getAuthorizationInfo().getUserSource();
		if (Strings.isNullOrEmpty(userSource)) {
			userSource = getDefaultDataSource();
		}
		String userTable = getAuthorizationInfo().getUserTable();
		return getDataContext(userSource).getTable(userTable);
	}

	public static boolean hasSourcePermissions(String source) {
		if (Strings.isNullOrEmpty(source)) {
			source = getDefaultDataSource();
		}
		return permissions.containsKey(source) && !permissions.get(source).isEmpty();
	}

	public static Map<String, List<TablePermission>> getSourcePermissions(String source) {
		if (Strings.isNullOrEmpty(source)) {
			source = getDefaultDataSource();
		}
		return permissions.get(source);
	}

	public static boolean hasTablePermissions(String source, String table) {
		if (hasSourcePermissions(source)) {
			Map<String, List<TablePermission>> sourceTablePermissions = getSourcePermissions(source);
			if (sourceTablePermissions.containsKey(table)) {
				return true;
			}
		}
		return false;
	}

	public static List<TablePermission> getTablePermissions(String source, String table) {
		return getSourcePermissions(source).get(table);
	}

	public static PermissionPolicy getPermissionPolicy() {
		return getConfiguration().permissionPolicy();
	}

	/**
	 * 获取表或者列等的别名, 根据配置的别名表达式
	 * 
	 * @param element
	 *            获取别名的元素, 比如表或者列等
	 * @return Alias
	 */
	@Deprecated
	public static String getAliasKey(String element) {
		JexlEngine jexl = new JexlBuilder().create();
		String jexlExp = App.Config.getString(GlobalConfig.TABLE_ALIAS_EXPRESSION);
		JexlExpression e = jexl.createExpression(jexlExp);
		JexlContext jc = new MapContext();
		jc.set("$", element);
		return e.evaluate(jc).toString();
	}

	public static Injector injector() {
		return injector;
	}

	public static Gson gson() {
		return gson;
	}

	public static void cleanup() {
		currentDataContext.remove();
	}

	public static class Config {

		private Config() {}

		// 默认配置文件名
		protected static final String DEFAULT_CONFIG_FILE = "air.json";
		// 默认数据表配置路径
		protected static final String DEFAULT_TABLE_CONFIG_PATH = "tables";
		// 全局配置
		private static final JsonObject configuration = new JsonObject();

		static {
			// 初始化默认属性
			configuration.addProperty(GlobalConfig.CONFIG_FILE, DEFAULT_CONFIG_FILE);
			configuration.add(GlobalConfig.DATASOURCES, JsonNull.INSTANCE);
			configuration.addProperty(GlobalConfig.SQL, false);
			configuration.addProperty(GlobalConfig.NATIVE, false);
			configuration.addProperty(GlobalConfig.DEFAULT_DATASOURCE, "");
			configuration.addProperty(GlobalConfig.TABLE_CONFIG_PATH, DEFAULT_TABLE_CONFIG_PATH);
			configuration.add(GlobalConfig.TABLE_CONFIG, JsonNull.INSTANCE);
			configuration.addProperty(GlobalConfig.ASSOCIATION_CONNECTOR, "__");
		}

		public static JsonElement get(String key) {
			return configuration.get(key);
		}

		public static JsonObject getJsonObject(String key) {
			return configuration.getAsJsonObject(key);
		}

		public static JsonArray getJsonArray(String key) {
			return configuration.getAsJsonArray(key);
		}

		public static String getString(String key) {
			if (configuration.has(key)) {
				JsonElement jsonElement = configuration.get(key);
				if (!jsonElement.isJsonNull()) {
					return jsonElement.getAsString();
				}
			}
			return "";
		}

		public static void set(String key, Object value) {
			if (value instanceof Boolean) {
				configuration.addProperty(key, (Boolean) value);
			} else if (value instanceof Character) {
				configuration.addProperty(key, (Character) value);
			} else if (value instanceof Number) {
				configuration.addProperty(key, ((Number) value));
			} else if (value instanceof String) {
				configuration.addProperty(key, value.toString());
			} else {
				configuration.add(key, (JsonElement) value);
			}
		}

		public static boolean has(String key) {
			return configuration.has(key);
		}
	}

	public static class Context {

		private Context() {}

		public static Dialect dialect() {
			return currentDataContext().getDialect();
		}
		
		public static boolean columnAliasEnabled() {
			return dialect().columnAliasEnabled();
		}

		public static Schema defaultSchema() {
			return currentDataContext().getDefaultSchema();
		}

		public static Schema getSchema(String schemaName) {
			return currentDataContext().getSchema(schemaName);
		}

		@Deprecated
		public static Table getTable(String nameOrAlias) {
			// 1 根据别名查询
			Schema schema = currentDataContext().getDefaultSchema();
			List<Table> tables = schema.getTables();
			for (Table table : tables) {
				if (table.getConfig().has(TableConfig.ALIAS)) { // 表配置文件配置的表别名
					String alias = table.getConfig().get(TableConfig.ALIAS).getAsString();
					if (alias.equals(nameOrAlias)) {
						return table;
					}
				} else if (Config.has(GlobalConfig.TABLE_ALIAS_EXPRESSION)) { // 全局配置的表别名
					if (nameOrAlias.equalsIgnoreCase(getAliasKey(table.getName()))) {
						return table;
					}
				}
			}
			// 2 根据实名查询
			return currentDataContext().getTable(nameOrAlias);
		}

		public static Table getTable(String schemaName, String tableName) {
			return currentDataContext().getTable(schemaName, tableName);
		}

		/**
		 * Table key in the data structure. Input or output
		 * 
		 * @param table
		 * @return Alias
		 */
		public static String getTableKey(Table table) {
			if (table.getConfig().has(TableConfig.ALIAS)) { // 表配置文件配置的表别名
				return table.getConfig().get(TableConfig.ALIAS).getAsString();
			} else if (Config.has(GlobalConfig.TABLE_ALIAS_EXPRESSION)) { // 全局配置的表别名
				return getAliasKey(table.getName());
			} else {
				return table.getName();
			}
		}

		public static Column getColumn(String tableNameOrAlias, String columnNameOrAlias) {
			return getColumn(getTable(tableNameOrAlias), columnNameOrAlias);
		}

		public static Column getColumn(Table table, String columnNameOrAlias) {
			if (table == null) {
				return null;
			}
			// 1 根据别名查询
			List<Column> columns = table.getColumns();
			for (Column column : columns) {
				if (column.getConfig().has(ColumnConfig.ALIAS)) {
					String alias = column.getConfig().get(ColumnConfig.ALIAS).getAsString();
					if (alias.equals(columnNameOrAlias)) {
						return column;
					}
				}
			}
			// 2 根据实名查询
			return currentDataContext().getColumn(table.getName(), columnNameOrAlias);
		}

		public static Column getColumn(String schemaName, String tableName, String columnName) {
			return currentDataContext().getColumn(schemaName, tableName, columnName);
		}

		public static String getColumnAlias(Column column) {
			if (column.getConfig().has(ColumnConfig.ALIAS)) {
				return column.getConfig().get(ColumnConfig.ALIAS).getAsString();
			} else {
				return column.getName();
			}
		}

		public static void destroy() {
			currentDataContext().destroy();
		}

		public static boolean addRelationship(Column primaryColumn, Column foreignColumn,
				AssociationType associationType) {
			return addRelationship(currentDataContext(), primaryColumn, foreignColumn, associationType);
		}

		public static boolean addRelationship(DataContext dataContext, Column primaryColumn, Column foreignColumn,
				AssociationType associationType) {
			return dataContext.addRelationship(primaryColumn, foreignColumn, associationType);
		}

		public static void addRelationship(String dataSourceName, String primaryTableName, String primaryColumnName,
				String foreignTableName, String foreignColumnName, AssociationType associationType) {
			DataContext dataContext = getDataContext(dataSourceName);
			Column primaryColumn = dataContext.getColumn(primaryTableName, primaryColumnName);
			Column foreignColumn = dataContext.getColumn(foreignTableName, foreignColumnName);
			boolean result = addRelationship(dataContext, primaryColumn, foreignColumn, associationType);
			if (result) {
				dataContext.cleanRelationshipCache();
			}
			String association = associationType.text();
			logger.info(
					"Add relationship {}: source[{}] primaryTable[{}] primaryColumn[{}] foreignTable[{}] foreignColumn[{}] association[{}]",
					result ? "success" : "fail", dataSourceName, primaryTableName, primaryColumnName, foreignTableName,
					foreignColumnName, association);
		}

		public static void deleteRelationship(String dataSourceName, String primaryTableName, String primaryColumnName,
				String foreignTableName, String foreignColumnName) {
			DataContext dataContext = getDataContext(dataSourceName);
			Column primaryColumn = dataContext.getColumn(primaryTableName, primaryColumnName);
			Column foreignColumn = dataContext.getColumn(foreignTableName, foreignColumnName);
			boolean result = dataContext.deleteRelationship(primaryColumn, foreignColumn);
			if (result) {
				dataContext.cleanRelationshipCache();
			}
			logger.info(
					"Add relationship {}: source[{}] primaryTable[{}] primaryColumn[{}] foreignTable[{}] foreignColumn[{}]",
					result ? "success" : "fail", dataSourceName, primaryTableName, primaryColumnName, foreignTableName,
					foreignColumnName);
		}

		public static void deleteRelationship(String dataSourceName, String primaryTableName, String foreignTableName) {
			DataContext dataContext = getDataContext(dataSourceName);
			Table primaryTable = dataContext.getTable(primaryTableName);
			Table foreignTable = dataContext.getTable(foreignTableName);
			boolean result = dataContext.deleteRelationship(primaryTable, foreignTable);
			if (result) {
				dataContext.cleanRelationshipCache();
			}
		}

		public static Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable) {
			return currentDataContext().getRelationships(primaryTable, foreignTable);
		}

		public static AssociationType getAssociationType(Table primaryTable, Table foreignTable) {
			return currentDataContext().getAssociationType(primaryTable, foreignTable);
		}

	}

	static class Action {

		private Action() {}

		public static String getTableAlias(Table table) {
			return getAlias(table != null ? table.getName() + "_" : "");
		}
		
		public static String getColumnAlias(Column column) {
			return getAlias(column != null ? column.getName() + "_" : "");
		}

		public static String getAlias(String prefix) {
			String alias = null;
			Dialect dialect = Context.dialect();
			if (dialect.tableAliasEnabled() && dialect.randomAliasEnabled()) {
				alias = Strings.nullToEmpty(prefix) + randomString(6);
			}
			return alias;
		}

		private static String randomString(int length) {
			RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
			return generator.generate(length);
		}

	}

}

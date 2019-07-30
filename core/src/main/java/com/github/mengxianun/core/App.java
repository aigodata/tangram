package com.github.mengxianun.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.text.RandomStringGenerator;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;
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

	private static Injector injector = Guice.createInjector(Stage.PRODUCTION, new AppModule());

	private static final Map<String, DataContext> dataContexts = new LinkedHashMap<>();
	// 当前线程的 DataContext
	private static final ThreadLocal<DataContext> currentDataContext = new ThreadLocal<>();

	private App() {}

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

	public static DataContext addDataContext(String name, DataContext dataContext) {
		return dataContexts.put(name, dataContext);
	}

	public static DataContext getDefaultDataContext() {
		return dataContexts.get(GlobalConfig.DEFAULT_DATASOURCE);
	}

	public static String getDefaultDataSource() {
		return Config.getString(GlobalConfig.DEFAULT_DATASOURCE);
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
			// 将第一个数据源设置为默认数据源
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

	public static Injector getInjector() {
		return injector;
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
			configuration.addProperty(GlobalConfig.UPSERT, false);
			configuration.addProperty(GlobalConfig.NATIVE, false);
			configuration.addProperty(GlobalConfig.LOG, false);
			configuration.addProperty(GlobalConfig.DEFAULT_DATASOURCE, "");
			configuration.addProperty(GlobalConfig.TABLE_CONFIG_PATH, DEFAULT_TABLE_CONFIG_PATH);
			configuration.add(GlobalConfig.TABLE_CONFIG, JsonNull.INSTANCE);
			// 预处理开关
			configuration.add(GlobalConfig.PRE_HANDLER, JsonNull.INSTANCE);
			// 权限控制
			configuration.add(GlobalConfig.AUTH_CONTROL, JsonNull.INSTANCE);
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
			return configuration.getAsJsonPrimitive(key).getAsString();
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

		public static String identifierQuoteString() {
			return currentDataContext().getIdentifierQuoteString();
		}

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

		public static Table getTable(String tableName) {
			return currentDataContext().getTable(tableName);
		}

		public static Table getTable(String schemaName, String tableName) {
			return currentDataContext().getTable(schemaName, tableName);
		}

		public static Column getColumn(String tableName, String columnName) {
			return currentDataContext().getColumn(tableName, columnName);
		}

		public static Column getColumn(String schemaName, String tableName, String columnName) {
			return currentDataContext().getColumn(schemaName, tableName, columnName);
		}

		public static void destroy() {
			currentDataContext().destroy();
		}

		public static void addRelationship(Column primaryColumn, Column foreignColumn,
				AssociationType associationType) {
			currentDataContext().addRelationship(primaryColumn, foreignColumn, associationType);
		}

		public static Set<Relationship> getRelationships(Table primaryTable, Table foreignTable) {
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

package com.github.mengxianun.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * Application center
 * 
 * @author mengxiangyun
 *
 */
public final class App {

	private App() {}

	private static final Map<String, DataContext> dataContexts = new LinkedHashMap<>();

	public static Map<String, DataContext> getDatacontexts() {
		return dataContexts;
	}

	public static Set<String> getDatacontextNames() {
		return dataContexts.keySet();
	}

	public static DataContext getDatacontext(String name) {
		return dataContexts.get(name);
	}

	public static boolean hasDataContext(String name) {
		return dataContexts.containsKey(name);
	}

	public static DataContext addDataContext(String name, DataContext dataContext) {
		return dataContexts.put(name, dataContext);
	}

	public static DataContext getDefaultDataContext() {
		return dataContexts.get(ConfigAttributes.DEFAULT_DATASOURCE);
	}

	static class Config {

		private Config() {}

		// 默认配置文件名
		protected static final String DEFAULT_CONFIG_FILE = "air.json";
		// 默认数据表配置路径
		protected static final String DEFAULT_TABLE_CONFIG_PATH = "tables";
		// 全局配置
		private static final JsonObject configuration = new JsonObject();

		static {
			// 初始化默认属性
			configuration.addProperty(ConfigAttributes.CONFIG_FILE, DEFAULT_CONFIG_FILE);
			configuration.add(ConfigAttributes.DATASOURCES, JsonNull.INSTANCE);
			configuration.addProperty(ConfigAttributes.UPSERT, false);
			configuration.addProperty(ConfigAttributes.NATIVE, false);
			configuration.addProperty(ConfigAttributes.LOG, false);
			configuration.addProperty(ConfigAttributes.DEFAULT_DATASOURCE, "");
			configuration.addProperty(ConfigAttributes.TABLE_CONFIG_PATH, DEFAULT_TABLE_CONFIG_PATH);
			configuration.add(ConfigAttributes.TABLE_CONFIG, JsonNull.INSTANCE);
			// 预处理开关
			configuration.add(ConfigAttributes.PRE_HANDLER, JsonNull.INSTANCE);
			// 权限控制
			configuration.add(ConfigAttributes.AUTH_CONTROL, JsonNull.INSTANCE);
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

}

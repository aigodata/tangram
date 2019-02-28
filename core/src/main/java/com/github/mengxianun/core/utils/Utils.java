package com.github.mengxianun.core.utils;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.ResultConverter;
import com.github.mengxianun.core.ResultConverterFactory;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.resutset.FailDataResultSet;
import com.github.mengxianun.core.resutset.FileDataResultSet;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.timer.TimerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.*;

public class Utils {

	private static List<ResultConverterFactory> converterFactories = null;
	private static List<TimerFactory> timerFactories = null;

	private static void converterFactoryInit() {
		if (converterFactories == null) {
			converterFactories = new ArrayList<>();

			ServiceLoader<ResultConverterFactory> serviceLoader = ServiceLoader.load(ResultConverterFactory.class);
			for (ResultConverterFactory factory : serviceLoader) {
				converterFactories.add(factory);
			}
		}
	}
	private static ResultConverterFactory converterFactory(String type) {
		converterFactoryInit();
		for (ResultConverterFactory factory : converterFactories) {
			if (factory.getType().equalsIgnoreCase(type)) {
				return factory;
			}
		}
		return null;
	}
	private static ResultConverter resultConverter(String type, JsonObject properties, List<Column> columns, JsonElement data) {
		return converterFactory(type).create(json2Map(properties), getDisplayName(columns), data);
	}
	public static DataResultSet export(String fileName, String type, JsonObject properties, JsonElement data, List<Column> columns) {
		try {
			return new FileDataResultSet(fileName.concat(Type.ConverterSuffix.EXCEL.suffix()), resultConverter(type, properties, columns, data).execute());
		} catch (Exception e) {
			return new FailDataResultSet(ResultStatus.TRANSLATION_FAILED);
		}
	}
	private static Map<String, Object> getDisplayName(List<Column> columns) {
		Map<String, Object> map = new HashMap<>();
		for (Column column : columns) {
			JsonObject config = column.getConfig();
			if (config.has("display")) {
				map.put(column.getName(), config.get("display"));
			}
		}
		return map;
	}
	public static List<Map<String, Object>> json2List(JsonElement data) {
		List<Map<String, Object>> list = new ArrayList<>();
		if (data.isJsonArray()) {
			for (JsonElement jsonElement : ((JsonArray) data)) {
				list.add(json2Map(jsonElement));
			}
		} else if (data.isJsonObject()) {
			list.add(json2Map(data));
		}
		return list;
	}
	public static Map<String, Object> json2Map(JsonElement jsonElement) {
		Map<String, Object> map = new LinkedHashMap<>();
		if (jsonElement.isJsonObject()) {
			for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}
	public static String replaceParams(String str, String key, String value) {

		str = str.replaceAll("\\$\\{" + key + "\\}", String.valueOf(value));
		return str;
	}
	public static synchronized String getUUID() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private static void timerFactoryInit() {
		if (timerFactories == null) {
			timerFactories = new ArrayList<>();

			ServiceLoader<TimerFactory> serviceLoader = ServiceLoader.load(TimerFactory.class);
			for (TimerFactory factory : serviceLoader) {
				timerFactories.add(factory);
			}
		}
	}
	private static TimerFactory timerFactory(String type) {
		timerFactoryInit();
		for (TimerFactory factory : timerFactories) {
			if (factory.getType().equalsIgnoreCase(type)) {
				return factory;
			}
		}
		return null;
	}
	private static void timerStart(String type, Integer aliveTimeMinute, Integer destroyTimeMinute) {
		timerFactory(type).create().start(aliveTimeMinute, destroyTimeMinute);
	}
	private static void timerStop(String type) {
		timerFactory(type).create().stop();
	}
	public static void refreshSecretKeyStart(Integer aliveTimeMinute, Integer destroyTimeMinute) {
		timerStart(Type.TimerType.ENCRYPT.type(), aliveTimeMinute, destroyTimeMinute);
	}
	public static void refreshSecretKeyStart() {
		timerStart(Type.TimerType.ENCRYPT.type(), null, null);
	}
	public static void refreshSecretKeyStop() {
		timerStop(Type.TimerType.ENCRYPT.type());
	}
}

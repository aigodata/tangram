package com.github.mengxianun.core;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class DialectFactory {

	// 数据库方言集合. key: 数据库关键字 value: 数据库方言类
	protected static Map<String, Class<? extends Dialect>> dialects = new HashMap<>();

	static {
		discoverFromClasspath();
	}

	public static void discoverFromClasspath() {
		final ServiceLoader<Dialect> serviceLoader = ServiceLoader.load(Dialect.class);
		for (Dialect dialect : serviceLoader) {
			dialects.put(dialect.getType(), dialect.getClass());
		}
	}

}

package com.github.mengxianun.jdbc;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.github.mengxianun.core.DataContextFactory;
import com.google.auto.service.AutoService;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@AutoService(DataContextFactory.class)
public final class JdbcDataContextFactory implements DataContextFactory {

	@Override
	public String getType() {
		return "jdbc";
	}

	@Override
	public JdbcDataContext create(JsonObject dataSourceJsonObject) {
		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, Object> dataSourceMap = new Gson().fromJson(dataSourceJsonObject, type);
		DataSource dataSource = createDataSource(dataSourceMap);
		return new JdbcDataContext(dataSource);
	}

	public DataSource createDataSource(String url, String username, String password) {
		Map<String, Object> dataSourceMap = new HashMap<>();
		dataSourceMap.put(DruidDataSourceFactory.PROP_URL, url);
		dataSourceMap.put(DruidDataSourceFactory.PROP_USERNAME, username);
		dataSourceMap.put(DruidDataSourceFactory.PROP_PASSWORD, password);
		return createDataSource(dataSourceMap);
	}

	public DataSource createDataSource(Map<String, Object> dataSourceMap) {
		// Add wall filters to prevent SQL injection
		String wall = "wall";
		if (dataSourceMap.containsKey(DruidDataSourceFactory.PROP_FILTERS)) {
			String filtersString = dataSourceMap.get(DruidDataSourceFactory.PROP_FILTERS).toString();
			if (Strings.isNullOrEmpty(filtersString)) {
				dataSourceMap.put(DruidDataSourceFactory.PROP_FILTERS, wall);
			} else if (!filtersString.contains(wall)) {
				filtersString += "," + wall;
				dataSourceMap.put(DruidDataSourceFactory.PROP_FILTERS, filtersString);
			}
		} else {
			dataSourceMap.put(DruidDataSourceFactory.PROP_FILTERS, wall);
		}
		try {
			return DruidDataSourceFactory.createDataSource(dataSourceMap);
		} catch (Exception e) {
			throw new JdbcDataException(e);
		}
	}

}

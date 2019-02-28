package com.github.mengxianun.jdbc;

import java.lang.reflect.Type;
import java.util.Map;

import javax.sql.DataSource;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.github.mengxianun.core.DataContextFactory;
import com.google.auto.service.AutoService;
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
		Map<String, String> dataSourceMap = new Gson().fromJson(dataSourceJsonObject, type);
		DataSource druidDataSource;
		try {
			druidDataSource = DruidDataSourceFactory.createDataSource(dataSourceMap);
		} catch (Exception e) {
			throw new JdbcDataException(e);
		}
		return new JdbcDataContext(druidDataSource);
	}

}

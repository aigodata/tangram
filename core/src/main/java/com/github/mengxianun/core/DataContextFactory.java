package com.github.mengxianun.core;

import com.google.gson.JsonObject;

public interface DataContextFactory {

	public String getType();

	public DataContext create(JsonObject dataSourceJsonObject);

}

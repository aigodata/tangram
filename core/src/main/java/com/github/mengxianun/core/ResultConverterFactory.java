package com.github.mengxianun.core;

import com.google.gson.JsonElement;

import java.util.Map;

public interface ResultConverterFactory {

	String getType();

	ResultConverter create(Map<String, Object> properties, Map<String, Object> header, JsonElement data);

}

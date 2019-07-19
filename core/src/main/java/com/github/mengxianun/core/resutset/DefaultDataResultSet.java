package com.github.mengxianun.core.resutset;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.ResultStatus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class DefaultDataResultSet extends AbstractDataResultSet {

	private final Object data;

	public DefaultDataResultSet(int code, String message) {
		super(code, message);
		this.data = null;
	}

	public DefaultDataResultSet(ResultStatus resultStatus) {
		super(resultStatus);
		this.data = null;
	}

	public DefaultDataResultSet(Object data) {
		super();
		this.data = data;
	}

	@Override
	public Object getData() {
		if (data == null) {
			return null;
		}
		if (data instanceof JsonElement) {
			JsonElement jsonData = (JsonElement) data;
			if (jsonData.isJsonArray()) {
				Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
				return new Gson().fromJson(jsonData, dataType);
			} else if (jsonData.isJsonObject()) {
				Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
				return new Gson().fromJson(jsonData, dataType);
			} else {
				Type dataType = new TypeToken<Object>() {}.getType();
				return new Gson().fromJson(jsonData, dataType);
			}
		} else {
			return data;
		}
	}

}

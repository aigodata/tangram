package com.github.mengxianun.core;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractDataResultSet implements DataResultSet {

	protected long took;

	protected JsonElement data;

	protected int code;
	protected String message;

	public AbstractDataResultSet() {
		this(ResultStatus.SUCCESS);
	}

	public AbstractDataResultSet(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public AbstractDataResultSet(long took, JsonElement data) {
		this(ResultStatus.SUCCESS.code(), ResultStatus.SUCCESS.message());
		this.took = took;
		this.data = data;
	}

	public AbstractDataResultSet(ResultStatus resultStatus) {
		this(resultStatus.code(), resultStatus.message());
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public Object getData() {
		if (data == null) {
			return null;
		}
		if (data.isJsonArray()) {
			Type dataType = new TypeToken<List<Map<String, Object>>>() {
			}.getType();
			return new Gson().fromJson(data, dataType);
		} else if (data.isJsonObject()) {
			Type dataType = new TypeToken<Map<String, Object>>() {
			}.getType();
			return new Gson().fromJson(data, dataType);
		} else {
			Type dataType = new TypeToken<Object>() {
			}.getType();
			return new Gson().fromJson(data, dataType);
		}
	}

	@Override
	public JsonElement getJsonData() {
		return data;
	}

	@Override
	public boolean succeed() {
		return this.code == ResultStatus.SUCCESS.code();
	}

	public long getTook() {
		return took;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

}

package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.ResultStatus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public abstract class AbstractDataResultSet implements DataResultSet {

	private final int code;
	private final String message;

	public AbstractDataResultSet() {
		this(ResultStatus.SUCCESS);
	}

	public AbstractDataResultSet(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public AbstractDataResultSet(ResultStatus resultStatus) {
		this(resultStatus.code(), resultStatus.message());
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public JsonElement getJsonData() {
		Gson gson = new GsonBuilder().serializeNulls().create();
		return gson.toJsonTree(getData());
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean succeed() {
		return this.code == ResultStatus.SUCCESS.code();
	}

}

package com.github.mengxianun.core.resutset;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.FileSummary;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractDataResultSet implements DataResultSet {

	private final int code;
	private final String message;
	protected final Summary summary;

	public AbstractDataResultSet(Summary summary) {
		this(ResultStatus.SUCCESS, summary);
	}

	public AbstractDataResultSet(ResultStatus resultStatus, Summary summary) {
		this(resultStatus.code(), resultStatus.message(), summary);
	}

	public AbstractDataResultSet(int code, String message, Summary summary) {
		this.code = code;
		this.message = message;
		this.summary = summary;
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
	public Object getData() {
		if (summary == null) {
			return null;
		}
		Object data = summary.getData();
		if (data instanceof JsonElement) {
			JsonElement jsonData = (JsonElement) data;
			if (jsonData.isJsonArray()) {
				Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
				return App.gson().fromJson(jsonData, dataType);
			} else if (jsonData.isJsonObject()) {
				Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
				return App.gson().fromJson(jsonData, dataType);
			} else {
				Type dataType = new TypeToken<Object>() {}.getType();
				return App.gson().fromJson(jsonData, dataType);
			}
		} else {
			return data;
		}
	}

	@Override
	public JsonElement getJsonData() {
		return App.gson().toJsonTree(getData());
	}

	@Override
	public boolean isFile() {
		return summary instanceof FileSummary;
	}

	@Override
	public String getFilename() {
		return ((FileSummary) summary).getFilename();
	}

	@Override
	public ByteArrayOutputStream getOutputStream() {
		return ((FileSummary) summary).getOutputStream();
	}

	@Override
	public boolean succeed() {
		return this.code == ResultStatus.SUCCESS.code();
	}

}

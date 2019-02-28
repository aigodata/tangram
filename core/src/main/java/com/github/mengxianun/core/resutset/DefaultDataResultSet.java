package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.AbstractDataResultSet;
import com.github.mengxianun.core.ResultStatus;
import com.google.gson.JsonElement;

public class DefaultDataResultSet extends AbstractDataResultSet {

	public DefaultDataResultSet() {
		super();
	}

	public DefaultDataResultSet(int code, String message) {
		super(code, message);
	}

	public DefaultDataResultSet(long took, JsonElement data) {
		super(took, data);
	}

	public DefaultDataResultSet(ResultStatus resultStatus) {
		super(resultStatus);
	}

}

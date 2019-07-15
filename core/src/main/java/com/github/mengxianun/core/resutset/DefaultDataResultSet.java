package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.ResultStatus;

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
		return data;
	}

}

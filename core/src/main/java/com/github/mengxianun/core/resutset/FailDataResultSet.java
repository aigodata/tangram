package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.AbstractDataResultSet;
import com.github.mengxianun.core.ResultStatus;

public class FailDataResultSet extends AbstractDataResultSet {

	public FailDataResultSet() {
		super();
	}

	public FailDataResultSet(int code, String message) {
		super(code, message);
	}

	public FailDataResultSet(ResultStatus resultStatus) {
		super(resultStatus);
	}

	public FailDataResultSet(ResultStatus resultStatus, Object... args) {
		super(resultStatus.code(), resultStatus.fill(args));
	}

}

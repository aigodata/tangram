package com.github.mengxianun.jdbc;

import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.exception.DataException;

public class JdbcDataException extends DataException {

	private static final long serialVersionUID = 1L;

	public JdbcDataException() {
		super();
	}

	public JdbcDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JdbcDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public JdbcDataException(String message) {
		super(message);
	}

	public JdbcDataException(Throwable cause) {
		super(cause);
	}

	public JdbcDataException(ResultStatus resultStatus) {
		super(resultStatus);
	}

	public JdbcDataException(String message, ResultStatus resultStatus) {
		super(message, resultStatus);
	}

	public JdbcDataException(String message, Throwable cause, ResultStatus resultStatus) {
		super(message, cause, resultStatus);
	}

}

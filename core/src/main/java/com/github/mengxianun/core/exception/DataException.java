package com.github.mengxianun.core.exception;

import com.github.mengxianun.core.ResultStatus;

public class DataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected int code;

	public DataException() {
		super();
	}

	public DataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataException(String message) {
		super(message);
	}

	public DataException(Throwable cause) {
		super(cause);
	}

	public DataException(ResultStatus resultStatus) {
		super(resultStatus.message());
		this.code = resultStatus.code();
	}

	public DataException(String message, ResultStatus resultStatus) {
		super(message);
		this.code = resultStatus.code();
	}

	public DataException(String message, Throwable cause, ResultStatus resultStatus) {
		super(message, cause);
		this.code = resultStatus.code();
	}

	public int getCode() {
		return code;
	}

}

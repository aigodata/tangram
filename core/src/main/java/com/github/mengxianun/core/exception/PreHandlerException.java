package com.github.mengxianun.core.exception;

import com.github.mengxianun.core.ResultStatus;

public class PreHandlerException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected int code;

	public PreHandlerException() {
		super();
	}

	public PreHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public PreHandlerException(String message, Throwable cause) {
		super(message, cause);
	}

	public PreHandlerException(String message) {
		super(message);
	}

	public PreHandlerException(Throwable cause) {
		super(cause);
	}

	public PreHandlerException(ResultStatus resultStatus) {
		super(resultStatus.message());
		this.code = resultStatus.code();
	}

	public PreHandlerException(String message, ResultStatus resultStatus) {
		super(message);
		this.code = resultStatus.code();
	}

	public PreHandlerException(String message, Throwable cause, ResultStatus resultStatus) {
		super(message, cause);
		this.code = resultStatus.code();
	}

	public int getCode() {
		return code;
	}

}

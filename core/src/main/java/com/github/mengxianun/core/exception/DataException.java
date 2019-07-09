package com.github.mengxianun.core.exception;

import com.github.mengxianun.core.ResultStatus;

public class DataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected final int code;

	public DataException() {
		this.code = 0;
	}

	public DataException(String message) {
		super(message);
		this.code = ResultStatus.SYSTEM_ERROR.code();
	}

	public DataException(String format, Object... args) {
		super(String.format(format, args));
		this.code = ResultStatus.SYSTEM_ERROR.code();
	}

	public DataException(String message, Throwable cause) {
		super(message, cause);
		this.code = ResultStatus.SYSTEM_ERROR.code();
	}

	public DataException(Throwable cause) {
		super(cause);
		this.code = ResultStatus.SYSTEM_ERROR.code();
	}

	public DataException(ResultStatus resultStatus) {
		super(resultStatus.message());
		this.code = resultStatus.code();
	}

	public DataException(ResultStatus resultStatus, Object... args) {
		super(resultStatus.fill(args));
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

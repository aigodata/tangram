package com.github.mengxianun.core.exception;

import com.github.mengxianun.core.ResultStatus;

/**
 * Json 数据结构异常
 * 
 * @author mengxiangyun
 *
 */
public class JsonDataException extends DataException {

	private static final long serialVersionUID = 1L;

	public JsonDataException() {
		super();
	}

	public JsonDataException(ResultStatus resultStatus) {
		super(resultStatus);
	}

	public JsonDataException(String message, ResultStatus resultStatus) {
		super(message, resultStatus);
	}

	public JsonDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsonDataException(String message, Throwable cause, ResultStatus resultStatus) {
		super(message, cause, resultStatus);
	}

	public JsonDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonDataException(String message) {
		super(message);
	}

	public JsonDataException(Throwable cause) {
		super(cause);
	}

}

package com.github.mengxianun.elasticsearch;

import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.exception.DataException;

public class ElasticsearchDataException extends DataException {

	private static final long serialVersionUID = 1L;

	public ElasticsearchDataException() {
		super();
	}

	public ElasticsearchDataException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ElasticsearchDataException(String message, Throwable cause) {
		super(message, cause);
	}

	public ElasticsearchDataException(String message) {
		super(message);
	}

	public ElasticsearchDataException(Throwable cause) {
		super(cause);
	}

	public ElasticsearchDataException(ResultStatus resultStatus) {
		super(resultStatus);
	}

	public ElasticsearchDataException(String message, ResultStatus resultStatus) {
		super(message, resultStatus);
	}

	public ElasticsearchDataException(String message, Throwable cause, ResultStatus resultStatus) {
		super(message, cause, resultStatus);
	}

}

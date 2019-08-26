package com.github.mengxianun.core.data.summary;

import java.io.ByteArrayOutputStream;

import com.github.mengxianun.core.data.AbstractSummary;

public class FileSummary extends AbstractSummary {

	private final QuerySummary querySummary;
	private final String filename;
	private final ByteArrayOutputStream outputStream;

	public FileSummary(QuerySummary querySummary, ByteArrayOutputStream outputStream) {
		super(querySummary.getAction(), querySummary.getData());
		this.querySummary = querySummary;
		this.filename = querySummary.getAction().getFilename();
		this.outputStream = outputStream;
	}

	public QuerySummary getQuerySummary() {
		return querySummary;
	}

	public String getFilename() {
		return filename;
	}

	public ByteArrayOutputStream getOutputStream() {
		return outputStream;
	}

}

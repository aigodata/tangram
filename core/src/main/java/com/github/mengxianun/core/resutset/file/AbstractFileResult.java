package com.github.mengxianun.core.resutset.file;

import java.io.InputStream;

import com.github.mengxianun.core.Action;

public abstract class AbstractFileResult implements FileResult {

	protected Object data;
	protected Action action;
	protected String filename;
	protected InputStream stream;

	public AbstractFileResult(Object data, Action action) {
		this.data = data;
		this.action = action;
		// 将第一个操作表的表名作为文件名
		this.filename = action.getTableItems().get(0).getTable().getName();
		convert();
	}

	/**
	 * 结果转换为流
	 */
	protected abstract void convert();

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public InputStream getStream() {
		return stream;
	}

}

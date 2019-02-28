package com.github.mengxianun.core.resutset;

import java.io.InputStream;

import com.github.mengxianun.core.AbstractDataResultSet;

public class FileDataResultSet extends AbstractDataResultSet {

	// 文件名
	private String filename;
	// 数据流
	private InputStream stream;

	public FileDataResultSet(String filename, InputStream stream) {
		this.filename = filename;
		this.stream = stream;
	}

}

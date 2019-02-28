package com.github.mengxianun.core.resutset.file;

import java.io.InputStream;

public interface FileResult {

	/**
	 * 获取文件名
	 * 
	 * @return
	 */
	public String getFilename();

	/**
	 * 获取数据流
	 * 
	 * @return
	 */
	public InputStream getStream();

}

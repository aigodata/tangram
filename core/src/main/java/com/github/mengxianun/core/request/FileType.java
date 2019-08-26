package com.github.mengxianun.core.request;

/**
 * 文件类型
 * 
 * @author mengxiangyun
 *
 */
public enum FileType {

	CSV, XLS, XLSX;

	public static FileType from(String type) {
		for (FileType resultType : values()) {
			if (resultType.toString().equalsIgnoreCase(type)) {
				return resultType;
			}
		}
		return null;
	}

}

package com.github.mengxianun.core;

/**
 * 返回结果类型
 * 
 * @author mengxiangyun
 *
 */
public enum ResultType {

	CSV;

	public static ResultType from(String type) {
		for (ResultType resultType : values()) {
			if (resultType.toString().equalsIgnoreCase(type)) {
				return resultType;
			}
		}
		return null;
	}

}

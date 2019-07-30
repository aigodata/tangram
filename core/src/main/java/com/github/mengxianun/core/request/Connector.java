package com.github.mengxianun.core.request;

/**
 * 请求 JSON 中 where 的条件连接符
 * 
 * @author mengxiangyun
 *
 */
public enum Connector {

	AND, OR;

	public static Connector from(String op) {
		for (Connector connector : values()) {
			if (connector.toString().equalsIgnoreCase(op)) {
				return connector;
			}
		}
		return null;
	}

}

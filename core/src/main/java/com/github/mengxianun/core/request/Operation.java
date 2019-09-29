package com.github.mengxianun.core.request;

/**
 * 操作
 * 
 * @author mengxiangyun
 *
 */
public enum Operation {

	DETAIL("detail"),
	QUERY("query"), 
	SELECT("select"), 
	SELECT_DISTINCT("select.distinct"), 
	INSERT("insert"), 
	UPDATE("update"), 
	DELETE("delete"), 
	TRANSACTION("transaction"), 
	STRUCT("struct"), 
	STRUCTS("structs"), 
	SQL("sql"), 
	NATIVE("native");

	private String value;

	private Operation(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public boolean isQuery() {
		return this == QUERY || this == SELECT || this == SELECT_DISTINCT || this == DETAIL;
	}

}

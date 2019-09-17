package com.github.mengxianun.core.request;

/**
 * 操作
 * 
 * @author mengxiangyun
 *
 */
public enum Operation {

	QUERY("query"), 
	SELECT("select"), 
	SELECT_DISTINCT("select.distinct"), 
	INSERT("insert"), 
	UPDATE("update"), 
	DELETE("delete"), 
	DETAIL("detail"), 
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

}

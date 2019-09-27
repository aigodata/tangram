package com.github.mengxianun.core.permission;


import com.google.common.base.Strings;

public enum TableAction {

	ALL("*"),
	QUERY("query", "select", "get"),
	ADD("add", "insert", "post"),
	UPDATE("update", "modify"),
	DELETE("delete", "remove"),
	OTHER();

	private String[] titles;

	TableAction(String... titles) {
		this.titles = titles;
	}

	public String[] titles() {
		return titles;
	}

	public static TableAction from(String action) {
		if (Strings.isNullOrEmpty(action)) {
			return ALL;
		}
		for (TableAction value : values()) {
			String[] titles = value.titles;
			for (String title : titles) {
				if (title.equalsIgnoreCase(action)) {
					return value;
				}
			}
		}
		return ALL;
	}

}

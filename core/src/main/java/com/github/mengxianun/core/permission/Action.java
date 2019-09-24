package com.github.mengxianun.core.permission;


import com.google.common.base.Strings;

public enum Action {

	ALL("*"),
	QUERY("query", "select", "get"),
	ADD("add", "insert", "post"),
	UPDATE("update", "modify"),
	DELETE("delete", "remove"),
	OTHER();

	private String[] titles;

	Action(String... titles) {
		this.titles = titles;
	}

	public String[] titles() {
		return titles;
	}

	public static Action from(String action) {
		if (Strings.isNullOrEmpty(action)) {
			return ALL;
		}
		Action[] values = values();
		for (Action value : values) {
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

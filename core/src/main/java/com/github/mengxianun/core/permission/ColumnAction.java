package com.github.mengxianun.core.permission;

import com.google.common.base.Strings;

public enum ColumnAction {

	ALL, READ, WRITE;

	public static ColumnAction from(String action) {
		if (Strings.isNullOrEmpty(action)) {
			return ALL;
		}
		for (ColumnAction value : values()) {
			if (value.name().equalsIgnoreCase(action)) {
				return value;
			}
		}
		return ALL;
	}

}

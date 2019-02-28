package com.github.mengxianun.core.json;

public enum JoinType {

	INNER, LEFT, RIGHT, FULL;

	public static JoinType from(String type) {
		for (JoinType joinType : values()) {
			if (joinType.toString().equalsIgnoreCase(type)) {
				return joinType;
			}
		}
		return null;
	}
}
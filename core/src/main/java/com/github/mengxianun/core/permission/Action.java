package com.github.mengxianun.core.permission;

public enum Action {

	ALL(0), SELECT(1), INSERT(2), UPDATE(3), DELETE(4), OTHER(9);

	private final int value;

	Action(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static Action from(int value) {
		for (Action action : values()) {
			if (action.value == value) {
				return action;
			}
		}
		return ALL;
	}

}

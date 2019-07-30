package com.github.mengxianun.core.config;

public enum AssociationType {

	ONE_TO_ONE("1_1"), ONE_TO_MANY("1_n"), MANY_TO_ONE("n_1"), MANY_TO_MANY("n_n");

	private String text;

	private AssociationType(String text) {
		this.text = text;
	}

	public String text() {
		return text;
	}

	public AssociationType reverse() {
		switch (this) {
		case ONE_TO_MANY:
			return MANY_TO_ONE;
		case MANY_TO_ONE:
			return ONE_TO_MANY;

		default:
			break;
		}
		return this;
	}

	public static AssociationType from(String text) {
		for (AssociationType type : values()) {
			if (type.text().equalsIgnoreCase(text)) {
				return type;
			}
		}
		return null;
	}

}

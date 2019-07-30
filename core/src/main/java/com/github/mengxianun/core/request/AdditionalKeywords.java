package com.github.mengxianun.core.request;


public enum AdditionalKeywords {

	ALIAS_KEY(" as ");

	private String value;

	private AdditionalKeywords(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

}

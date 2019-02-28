package com.github.mengxianun.core.json;

public enum Template {

	CSV;
	
	public static Template from(String name) {
		for (Template template : values()) {
			// 不区分大小写
			if (template.toString().equalsIgnoreCase(name)) {
				return template;
			}
		}
		return null;
	}

}

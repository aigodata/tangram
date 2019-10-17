package com.github.mengxianun.jdbc.schema;

import java.util.Map;

import com.github.mengxianun.core.schema.AbstractSchema;

public class JdbcSchema extends AbstractSchema {

	private final String catalog;

	public JdbcSchema(String name, String catalog) {
		super(name);
		this.catalog = catalog;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = super.getInfo();
		info.put("catalog", catalog);
		return info;
	}

	public String getCatalog() {
		return catalog;
	}

}

package com.github.mengxianun.elasticsearch.dialect;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.dialect.AbstractDialect;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class ElasticsearchDialect extends AbstractDialect {

	@Override
	public String getType() {
		return "elasticsearch";
	}

	@Override
	public boolean schemaPrefix() {
		return false;
	}

	@Override
	public boolean validTableExists() {
		return false;
	}

	@Override
	public boolean tableAliasEnabled() {
		return false;
	}

	@Override
	public boolean randomAliasEnabled() {
		return false;
	}

	@Override
	public String processKeyword(String keyword) {
		return '"' + keyword + '"';
	}

}

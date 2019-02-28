package com.github.mengxianun.elasticsearch.dialect;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.SQLBuilder;
import com.github.mengxianun.elasticsearch.ElasticsearchSQLBuilder;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class ElasticsearchDialect implements Dialect {

	@Override
	public String getType() {
		return "elasticsearch";
	}

	@Override
	public Class<? extends SQLBuilder> getSQLBuilder() {
		return ElasticsearchSQLBuilder.class;
	}

	@Override
	public boolean assignDatabase() {
		return false;
	}

	@Override
	public boolean validTableExists() {
		return false;
	}

	@Override
	public boolean quoteTable() {
		return false;
	}

	@Override
	public boolean tableAliasEnabled() {
		return false;
	}

	@Override
	public boolean columnAliasEnabled() {
		return true;
	}

	@Override
	public boolean randomAliasEnabled() {
		return false;
	}

}

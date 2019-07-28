package com.github.mengxianun.jdbc.dialect;

import com.github.mengxianun.core.Dialect;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class PostgreSQLDialect implements Dialect {

	@Override
	public String getType() {
		return "postgresql";
	}

	//	@Override
	//	public boolean assignDatabase() {
	//		return false;
	//	}

	@Override
	public boolean quoteTable() {
		return false;
	}

	@Override
	public String getJsonPlaceholder() {
		return "?::json";
	}

}

package com.github.mengxianun.jdbc.dialect;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class PostgreSQLDialect extends JdbcDialect {

	public PostgreSQLDialect(JdbcDataContext jdbcDataContext) {
		super(jdbcDataContext);
	}

	@Override
	public String getType() {
		return "postgresql";
	}

	@Override
	public String getJsonPlaceholder() {
		return "?::json";
	}

	@Override
	protected String getTimeLikeColumn(String column) {
		return column + "::text";
	}

}

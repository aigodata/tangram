package com.github.mengxianun.jdbc.dialect;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class H2Dialect extends JdbcDialect {

	public H2Dialect(JdbcDataContext jdbcDataContext) {
		super(jdbcDataContext);
	}

	@Override
	public String getType() {
		return "h2";
	}

	@Override
	public long offset() {
		return 0;
	}

}

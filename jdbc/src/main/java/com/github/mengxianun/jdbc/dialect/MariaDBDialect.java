package com.github.mengxianun.jdbc.dialect;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class MariaDBDialect extends JdbcDialect {

	public MariaDBDialect(JdbcDataContext jdbcDataContext) {
		super(jdbcDataContext);
	}

	@Override
	public String getType() {
		return "mariadb";
	}

}

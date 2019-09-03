package com.github.mengxianun.jdbc.dialect;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.dialect.Function;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.github.mengxianun.jdbc.dialect.function.MySQLFunction;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class MySQLDialect extends JdbcDialect {

	public MySQLDialect(JdbcDataContext jdbcDataContext) {
		super(jdbcDataContext, functions());

	}

	@Override
	public String getType() {
		return "mysql";
	}

	static Map<String, Function> functions() {
		return Arrays.stream(MySQLFunction.values()).collect(Collectors.toMap(MySQLFunction::name, e -> e));
	}

}

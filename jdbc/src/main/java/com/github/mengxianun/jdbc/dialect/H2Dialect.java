package com.github.mengxianun.jdbc.dialect;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.dialect.Function;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.github.mengxianun.jdbc.dialect.function.H2Function;
import com.google.auto.service.AutoService;

@AutoService(Dialect.class)
public class H2Dialect extends JdbcDialect {

	public H2Dialect(JdbcDataContext jdbcDataContext) {
		super(jdbcDataContext, functions());
	}

	@Override
	public String getType() {
		return "h2";
	}

	@Override
	public long offset() {
		return 0;
	}

	static Map<String, Function> functions() {
		return Arrays.stream(H2Function.values()).collect(Collectors.toMap(H2Function::name, e -> e));
	}

}

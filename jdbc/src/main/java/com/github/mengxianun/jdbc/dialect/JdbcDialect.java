package com.github.mengxianun.jdbc.dialect;

import java.util.Map;

import com.github.mengxianun.core.dialect.AbstractDialect;
import com.github.mengxianun.core.dialect.Function;
import com.github.mengxianun.jdbc.JdbcDataContext;

public class JdbcDialect extends AbstractDialect {

	protected final JdbcDataContext jdbcDataContext;

	public JdbcDialect(JdbcDataContext jdbcDataContext) {
		this.jdbcDataContext = jdbcDataContext;
	}

	public JdbcDialect(JdbcDataContext jdbcDataContext, Map<String, Function> functions) {
		super(functions);
		this.jdbcDataContext = jdbcDataContext;
	}

	@Override
	public String getType() {
		return "";
	}

	@Override
	public String processKeyword(String keyword) {
		String identifierQuoteString = jdbcDataContext.getIdentifierQuoteString();
		return identifierQuoteString + keyword + identifierQuoteString;
	}
	

}

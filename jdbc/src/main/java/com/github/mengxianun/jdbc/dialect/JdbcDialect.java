package com.github.mengxianun.jdbc.dialect;

import com.github.mengxianun.core.dialect.AbstractDialect;
import com.github.mengxianun.jdbc.JdbcDataContext;

public class JdbcDialect extends AbstractDialect {

	protected final JdbcDataContext jdbcDataContext;

	public JdbcDialect(JdbcDataContext jdbcDataContext) {
		super();
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

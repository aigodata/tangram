package com.github.mengxianun.jdbc.dialect.function;

import com.github.mengxianun.core.dialect.Function;

public enum JdbcFunction implements Function {
	;

	@Override
	public String convert(String func, String args) {
		throw new UnsupportedOperationException();
	}

}

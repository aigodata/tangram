package com.github.mengxianun.core;

public class DefaultDialect implements Dialect {

	@Override
	public String getType() {
		return "";
	}

	@Override
	public boolean quoteTable() {
		return false;
	}

	@Override
	public boolean tableAliasEnabled() {
		return true;
	}

	@Override
	public boolean columnAliasEnabled() {
		return true;
	}

}

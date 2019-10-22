package com.github.mengxianun.core.parser;

import org.apache.commons.text.RandomStringGenerator;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;

public abstract class AbstractActionParser implements ActionParser {

	protected final SimpleInfo simpleInfo;
	protected final DataContext dataContext;

	public AbstractActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		this.simpleInfo = simpleInfo;
		this.dataContext = dataContext;
	}

	protected String getTableAlias(Table table) {
		return getAlias(table != null ? table.getName() + "_" : "");
	}

	protected String getColumnAlias(Column column) {
		return getAlias(column != null ? column.getName() + "_" : "");
	}

	protected String getAlias(String prefix) {
		Dialect dialect = dataContext.getDialect();
		if (dialect.tableAliasEnabled() && dialect.randomAliasEnabled()) {
			return Strings.nullToEmpty(prefix) + randomString(6);
		}
		return null;
	}

	private static String randomString(int length) {
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
		return generator.generate(length);
	}

}

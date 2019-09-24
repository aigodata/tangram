package com.github.mengxianun.core.parser;

import java.util.List;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.text.RandomStringGenerator;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.App.Config;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.config.ColumnConfig;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.config.TableConfig;
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

	protected Table getTable(String nameOrAlias) {
		// 1 find by table alias
		List<Table> tables = dataContext.getDefaultSchema().getTables();
		for (Table table : tables) {
			if (table.getConfig().has(TableConfig.ALIAS)) { // table config
				String alias = table.getConfig().get(TableConfig.ALIAS).getAsString();
				if (alias.equals(nameOrAlias)) {
					return table;
				}
			} else if (Config.has(GlobalConfig.TABLE_ALIAS_EXPRESSION)) { // global config
				if (nameOrAlias.equalsIgnoreCase(getTableAliasKey(table.getName()))) {
					return table;
				}
			}
		}
		// 2 find by table name
		return dataContext.getTable(nameOrAlias);
	}

	private String getTableAliasKey(String element) {
		JexlEngine jexl = new JexlBuilder().create();
		String jexlExp = App.Config.getString(GlobalConfig.TABLE_ALIAS_EXPRESSION);
		JexlExpression e = jexl.createExpression(jexlExp);
		JexlContext jc = new MapContext();
		jc.set("$", element);
		return e.evaluate(jc).toString();
	}

	protected Column getColumn(String tableNameOrAlias, String columnNameOrAlias) {
		Table table = getTable(tableNameOrAlias);
		if (table == null) {
			return null;
		}
		// 1 find by alias
		List<Column> columns = table.getColumns();
		for (Column column : columns) {
			if (column.getConfig().has(ColumnConfig.ALIAS)) {
				String alias = column.getConfig().get(ColumnConfig.ALIAS).getAsString();
				if (alias.equals(columnNameOrAlias)) {
					return column;
				}
			}
		}
		// 2 find by name
		return dataContext.getColumn(table.getName(), columnNameOrAlias);
	}

}

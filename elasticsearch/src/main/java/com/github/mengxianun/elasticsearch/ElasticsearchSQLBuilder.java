package com.github.mengxianun.elasticsearch;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.SQLBuilder;

/**
 * Elasticsearch SQL 框架 SQL 语句不支持占位符方式, 所以这里将 SQL 改成拼接方式
 * 
 * @author mengxiangyun
 *
 */
public class ElasticsearchSQLBuilder extends SQLBuilder {

	public ElasticsearchSQLBuilder(Action action) {
		super(action);
	}

	@Override
	public String countSql() {
		StringBuilder countBuilder = new StringBuilder();
		// 去掉 limit 部分的SQL
		String notLimitSql = sql.split(SQLBuilder.PREFIX_LIMIT)[0];
		countBuilder.append(PREFIX_SELECT).append(COUNT).append(ALIAS_KEY).append("count")
				.append(PREFIX_FROM).append(notLimitSql.split(PREFIX_FROM, 2)[1]);
		return countBuilder.toString();
	}

	// @Override
	// public List<Object> countParams() {
	// return Collections.emptyList();
	// }

	@Override
	public String getSql() {
		int cols = params.size();
		Object[] values = new Object[cols];
		for (int i = 0; i < cols; i++) {
			Object value = params.get(i);
			if (value instanceof Date) {
				values[i] = "'" + value + "'";
			} else if (value instanceof String) {
				values[i] = "'" + value + "'";
			} else if (value instanceof Boolean) {
				values[i] = (Boolean) value ? 1 : 0;
			} else {
				values[i] = value;
			}
		}
		return String.format(sql.replaceAll("\\?", "%s"), values);
	}

	@Override
	public List<Object> getParams() {
		return Collections.emptyList();
	}

}

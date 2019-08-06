package com.github.mengxianun.core;

import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SQLParser {

	private SQLParser() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * 填充 SQL 占位符
	 * 
	 * @param sql
	 * @param params
	 * @return Full SQL
	 * @throws SQLException
	 */
	public static String fill(String sql, Object... params) throws SQLException {
		if (params == null || params.length == 0) {
			return sql;
		}
		if (!match(sql, params)) {
			throw new SQLException(
					"The number of placeholders for SQL statements does not match the number of parameters");
		}
		int cols = params.length;
		Object[] values = new Object[cols];
		System.arraycopy(params, 0, values, 0, cols);
		for (int i = 0; i < cols; i++) {
			Object value = values[i];
			if (value instanceof Date) {
				values[i] = "'" + value + "'";
			} else if (value instanceof String) {
				values[i] = "'" + value + "'";
			} else if (value instanceof Boolean) {
				values[i] = (Boolean) value ? 1 : 0;
			}
		}
		return String.format(sql.replaceAll("\\?", "%s"), values);
	}

	/**
	 * SQL占位符和参数个数是否匹配
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	private static boolean match(String sql, Object[] params) {
		if (params == null || params.length == 0)
			return true;
		Matcher m = Pattern.compile("(\\?)").matcher(sql);
		int count = 0;
		while (m.find()) {
			count++;
		}
		return count == params.length;
	}

}

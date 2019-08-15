package com.github.mengxianun.jdbc.dbutils.processor;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;

public class JdbcRowProcessor extends BasicRowProcessor {

	@Override
	public Object[] toArray(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();
		Object[] result = new Object[cols];

		for (int i = 0; i < cols; i++) {
			int columnType = meta.getColumnType(i + 1);
			if (columnType == Types.ARRAY) {
				Array array = rs.getArray(i + 1);
				result[i] = array == null ? null : array.getArray();
			} else {
				result[i] = rs.getObject(i + 1);
			}
		}

		return result;
	}

	@Override
	public Map<String, Object> toMap(ResultSet rs) throws SQLException {
		Map<String, Object> result = new LinkedHashMap<>();
		ResultSetMetaData meta = rs.getMetaData();
		int cols = meta.getColumnCount();

		for (int i = 1; i <= cols; i++) {
			String columnName = meta.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length()) {
				columnName = meta.getColumnName(i);
			}
			Object value = null;
			int columnType = meta.getColumnType(i);
			if (columnType == Types.ARRAY) {
				Array array = rs.getArray(i);
				value = array == null ? null : array.getArray();
			} else {
				value = rs.getObject(i);
			}
			result.put(columnName, value);
		}

		return result;
	}

}

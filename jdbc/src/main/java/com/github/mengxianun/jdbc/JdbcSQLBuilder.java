package com.github.mengxianun.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.SQLBuilder;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.jdbc.schema.JdbcColumnType;

public class JdbcSQLBuilder extends SQLBuilder {

	public JdbcSQLBuilder(Action action) {
		super(action);
	}

	@Override
	public Object processColumnValue(Column column, Object value) {
		if (value == null) {
			return null;
		}
		JdbcColumnType columnType = (JdbcColumnType) column.getType();
		if (columnType.isArray() && value.getClass().isArray()) {
			JdbcDataContext jdbcDataContext = (JdbcDataContext) dataContext;
			try (Connection connection = jdbcDataContext.getConnection()) {
				value = connection.createArrayOf(columnType.getTypeName(), (Object[]) value);
			} catch (SQLException e) {
				throw new JdbcDataException("Create array type value failed", e);
			}
		}
		return value;
	}

}

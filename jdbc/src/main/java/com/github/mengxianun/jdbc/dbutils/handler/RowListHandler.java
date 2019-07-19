package com.github.mengxianun.jdbc.dbutils.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.jdbc.dbutils.processor.RowProcessor;

public class RowListHandler extends AbstractListHandler<Row> {

	private final RowProcessor convert;

	public RowListHandler() {
		this(new RowProcessor());
	}

	public RowListHandler(RowProcessor convert) {
		this.convert = convert;
	}

	@Override
	protected Row handleRow(ResultSet rs) throws SQLException {
		return convert.toRow(rs);
	}

}

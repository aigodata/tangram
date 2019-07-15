package com.github.mengxianun.jdbc.dbutils.handler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.handlers.AbstractListHandler;

import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.jdbc.dbutils.processor.RowProcessor;

public class RowListHandler extends AbstractListHandler<Row> {

	private final RowProcessor convert;
	private final List<ColumnItem> items;


	public RowListHandler(List<ColumnItem> items) {
		this(new RowProcessor(), items);
	}

	public RowListHandler(RowProcessor convert, List<ColumnItem> items) {
		this.convert = convert;
		this.items = items;
	}

	@Override
	protected Row handleRow(ResultSet rs) throws SQLException {
		return convert.toRow(rs, items);
	}

}

package com.github.mengxianun.jdbc.dbutils.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.SimpleDataSetHeader;
import com.github.mengxianun.core.item.ColumnItem;

public class RowProcessor {

	public Row toRow(ResultSet rs, List<ColumnItem> items) throws SQLException {
		SimpleDataSetHeader header = new SimpleDataSetHeader(items);
		List<Object> values = new ArrayList<>();
		IntStream.range(0, rs.getMetaData().getColumnCount()).forEach(values::add);
		return new DefaultRow(header, values.toArray());
	}

}

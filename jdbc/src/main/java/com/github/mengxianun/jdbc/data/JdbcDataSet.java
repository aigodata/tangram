package com.github.mengxianun.jdbc.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.data.AbstractDataSet;
import com.github.mengxianun.core.data.DataSetHeader;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;

public class JdbcDataSet extends AbstractDataSet {
	
	private final List<Object[]> values;

	public JdbcDataSet(DataSetHeader header, List<Object[]> values) {
		super(header, values);
		this.values = values;
	}

	public JdbcDataSet(List<Map<String, Object>> values) {
		super(null, values);
		this.values = null;
	}

	@Override
	public List<Row> toRows() {
		return values.stream().map(e -> new DefaultRow(header, e)).collect(Collectors.toList());
	}

	@Override
	public List<Object[]> toObjectArrays() {
		return values;
	}

}

package com.github.mengxianun.jdbc.data;

import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;

public class JdbcQuerySummary extends QuerySummary {
	
	private final List<Object[]> values;

	public JdbcQuerySummary(Action action, List<Object[]> values) {
		super(action, values);
		this.values = values;
	}

	@Override
	public List<Row> toRows() {
		return values.stream().map(e -> new DefaultRow(header, e)).collect(Collectors.toList());
	}

}

package com.github.mengxianun.jdbc.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;

public class JdbcQuerySummary extends QuerySummary {
	
	private final List<Object[]> jdbcValues;

	public JdbcQuerySummary(Action action, List<Object[]> jdbcValues) {
		super(action, null);
		this.jdbcValues = jdbcValues;
	}

	@Override
	public List<Row> toRows() {
		return jdbcValues.stream().map(e -> new DefaultRow(header, e)).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> toValues() {
		return Collections.emptyList();
	}

}

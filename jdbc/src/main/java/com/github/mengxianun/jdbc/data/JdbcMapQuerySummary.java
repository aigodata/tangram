package com.github.mengxianun.jdbc.data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;

public class JdbcMapQuerySummary extends QuerySummary {

	private final List<Map<String, Object>> mapValues;

	public JdbcMapQuerySummary(Action action, List<Map<String, Object>> values) {
		super(action, values);
		this.mapValues = values;
	}

	@Override
	public List<Row> toRows() {
		return values.stream().map(e -> new DefaultRow(header, e.values().toArray())).collect(Collectors.toList());
	}

	@Override
	public List<Map<String, Object>> toValues() {
		return mapValues;
	}

}

package com.github.mengxianun.core.data.summary;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.data.Row;

public class PageSummary extends QuerySummary {

	private final long start;
	private final long end;
	private final long total;
	private final List<Map<String, Object>> values;

	public PageSummary(Action action, long start, long end, long total, List<Map<String, Object>> values) {
		super(action, values);
		this.start = start;
		this.end = end;
		this.total = total;
		this.values = values;
	}

	@Override
	public Map<String, Object> getData() {
		Map<String, Object> pageResult = new LinkedHashMap<>();
		pageResult.put(ResultAttributes.START, start);
		pageResult.put(ResultAttributes.END, end);
		pageResult.put(ResultAttributes.TOTAL, total);
		pageResult.put(ResultAttributes.DATA, values);
		return pageResult;
	}

	@Override
	public List<Row> toRows() {
		throw new UnsupportedOperationException();
	}

}

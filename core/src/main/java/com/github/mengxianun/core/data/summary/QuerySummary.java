package com.github.mengxianun.core.data.summary;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.data.AbstractSummary;
import com.github.mengxianun.core.data.DataSetHeader;
import com.github.mengxianun.core.data.DefaultDataSetHeader;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.item.LimitItem;

public abstract class QuerySummary extends AbstractSummary {

	protected DataSetHeader header;
	private List<Row> rows;
	private int index;

	protected long total;
	private List<Map<String, Object>> values;

	public QuerySummary(Action action, Object data) {
		this(action, data, -1);
	}

	public QuerySummary(Action action, Object data, long total) {
		super(action, data);
		this.header = new DefaultDataSetHeader(action.getColumnItems());
		this.total = total;
	}

	public Row getRow() {
		return getRows().get(index++);
	}

	public List<Row> getRows() {
		if (rows == null) {
			rows = toRows();
		}
		return rows;
	}

	public abstract List<Row> toRows();

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<Map<String, Object>> getValues() {
		return values;
	}

	public void setValues(List<Map<String, Object>> values) {
		this.values = values;
	}

	@Override
	public Object getData() {
		Object data = values;
		if (action.isDetail()) {
			data = values.isEmpty() ? Collections.emptyMap() : values.get(0);
		}
		if (action.isLimit()) {
			LimitItem limitItem = action.getLimitItem();
			long start = limitItem.getStart();
			long end = limitItem.getEnd();
			Map<String, Object> pageResult = new LinkedHashMap<>();
			pageResult.put(ResultAttributes.START, start);
			pageResult.put(ResultAttributes.END, end);
			pageResult.put(ResultAttributes.TOTAL, total);
			pageResult.put(ResultAttributes.DATA, values);
			data = pageResult;
		}
		return data;
	}

}

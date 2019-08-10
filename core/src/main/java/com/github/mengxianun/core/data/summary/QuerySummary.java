package com.github.mengxianun.core.data.summary;

import java.util.List;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.AbstractSummary;
import com.github.mengxianun.core.data.DataSetHeader;
import com.github.mengxianun.core.data.DefaultDataSetHeader;
import com.github.mengxianun.core.data.Row;

public abstract class QuerySummary extends AbstractSummary {

	protected DataSetHeader header;
	private List<Row> rows;
	private int index;
	protected long total;

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

}

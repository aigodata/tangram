package com.github.mengxianun.core.data;

import java.util.List;

public abstract class AbstractDataSet implements DataSet {
	
	protected final DataSetHeader header;
	private final Object nativeData;
	private List<Row> rows;
	private int index;

	public AbstractDataSet(DataSetHeader header, Object nativeData) {
		this.header = header;
		this.nativeData = nativeData;
	}

	@Override
	public Row getRow() {
		if (rows == null) {
			rows = toRows();
		}
		return rows.get(index++);
	}

	@Override
	public Object getNativeData() {
		return nativeData;
	}

}

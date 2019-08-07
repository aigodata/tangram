package com.github.mengxianun.core.data;

import java.util.List;
import java.util.stream.Collectors;

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
		return getRows().get(index++);
	}

	@Override
	public List<Row> getRows() {
		if (rows == null) {
			rows = toRows();
		}
		return rows;
	}

	@Override
	public List<Object[]> toObjectArrays() {
		return toRows().stream().map(Row::getValues).collect(Collectors.toList());
	}

	@Override
	public Object getNativeData() {
		return nativeData;
	}

}

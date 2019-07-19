package com.github.mengxianun.core.data;

import java.util.List;

public interface DataSet {

	public Row getRow();

	public List<Row> toRows();

	public List<Object[]> toObjectArrays();

	public Object getNativeData();

}

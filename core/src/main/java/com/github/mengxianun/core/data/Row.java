package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;

public interface Row {

	public Object getValue(ColumnItem item);

	public Object getValue(Column column);

	public Object getValue(String columnName);

	public Object getValue(int index);

	public Object[] getValues();

	public List<ColumnItem> getColumnItems();

	public int size();

}

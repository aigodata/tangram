package com.github.mengxianun.core.data;

import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;

public interface Row {

	Header getHeader();

	Object getValue(ColumnItem item);

	Object getValue(Column column);

	Object getValue(String columnName);

	Object getValue(int index);

	Object[] getValues();

	List<ColumnItem> getColumnItems();

	int size();

}

package com.github.mengxianun.jdbc.schema;

import java.util.Map;

import com.github.mengxianun.core.schema.AbstractColumn;
import com.github.mengxianun.core.schema.ColumnType;
import com.github.mengxianun.core.schema.Table;

public class JdbcColumn extends AbstractColumn {

	private final boolean nullable;
	private final String remarks;
	private final int columnSize;

	public JdbcColumn(String name, ColumnType columnType, Table table, boolean nullable, String remarks,
			int columnSize) {
		super(name, columnType, table);
		this.nullable = nullable;
		this.remarks = remarks;
		this.columnSize = columnSize;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = super.getInfo();
		info.put("nullable", nullable);
		info.put("remarks", remarks);
		info.put("size", columnSize);
		return info;
	}

	public boolean isNullable() {
		return nullable;
	}


	public String getRemarks() {
		return remarks;
	}

	public int getColumnSize() {
		return columnSize;
	}

}

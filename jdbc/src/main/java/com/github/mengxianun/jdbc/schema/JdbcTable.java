package com.github.mengxianun.jdbc.schema;

import java.util.Map;

import com.github.mengxianun.core.schema.AbstractTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.TableType;

public class JdbcTable extends AbstractTable {

	private final String remarks;

	public JdbcTable(String name, TableType type, Schema schema, String remarks) {
		super(name, type, schema);
		this.remarks = remarks;
	}

	@Override
	public Map<String, Object> getInfo() {
		Map<String, Object> info = super.getInfo();
		info.put("remarks", remarks);
		return info;
	}

	public String getRemarks() {
		return remarks;
	}

}

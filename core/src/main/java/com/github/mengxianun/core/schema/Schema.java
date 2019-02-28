package com.github.mengxianun.core.schema;

import java.util.List;

public interface Schema extends Name {

	public int getTableCount();

	public List<Table> getTables();

	public List<String> getTableNames();

	public Table getTableByName(String tableName);

}

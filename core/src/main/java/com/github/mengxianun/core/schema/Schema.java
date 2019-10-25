package com.github.mengxianun.core.schema;

import java.util.List;
import java.util.Map;

public interface Schema extends Name {

	public int getTableCount();

	public List<Table> getTables();

	public List<String> getTableNames();

	public Table getTable(String nameOrAlias);

	public Table getTableByName(String tableName);

	public void addTable(Table table);

	public void removeTable(Table table);

	public Map<String, Object> getInfo();

}

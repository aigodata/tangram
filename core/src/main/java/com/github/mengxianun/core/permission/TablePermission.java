package com.github.mengxianun.core.permission;

import java.util.List;

public class TablePermission {

	private String source;
	private String table;
	private Object id;
	private Action action;
	private List<Condition> conditions;

	public TablePermission() {}

	public TablePermission(String source, String table, Object id, String action, List<Condition> conditions) {
		this(source, table, id, Action.from(action), conditions);
	}

	public TablePermission(String source, String table, Object id, Action action, List<Condition> conditions) {
		this.source = source;
		this.table = table;
		this.id = id;
		this.action = action;
		this.conditions = conditions;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}

}

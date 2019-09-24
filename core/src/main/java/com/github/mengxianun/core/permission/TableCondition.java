package com.github.mengxianun.core.permission;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.request.Operator;

public class TableCondition implements Condition {

	private String source;
	private String table;
	private String column;
	private Object value;
	private Operator op;

	public TableCondition() {}

	public TableCondition(String source, String table, String column, Object value) {
		this.source = source;
		this.table = table;
		this.column = column;
		this.value = value;
		this.op = Operator.EQUAL;
	}

	@Override
	public void process(Action action) {

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

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Operator getOp() {
		return op;
	}

	public void setOp(Operator op) {
		this.op = op;
	}

}

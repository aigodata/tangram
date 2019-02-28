package com.github.mengxianun.core.json;

/**
 * 请求 JSON 中 where 的条件运算符
 * 
 * @author mengxiangyun
 *
 */
public enum Operator {

	// 等于
	EQUAL("=", "="),
	// 等于
	STRONG_EQUAL("==", "="),
	// 不等于
	NOT_EQUAL("!=", "<>"),
	// 大于
	GT(">", ">"),
	// 大于等于
	GTE(">=", ">="),
	// 小于
	LT("<", "<"),
	// 小于等于
	LTE("<=", "<="),
	// in
	IN(",", "in"),
	// not in
	NOT_IN(",", "not in"),
	// between
	BETWEEN("~", "between"),
	// like
	LIKE("%=", "like"),
	// not like
	NOT_LIKE("!%=", "not like"),
	// not
	NOT("!", "not");

	/*
	 * 运算符号
	 */
	private String op;

	/*
	 * 运算符对应的SQL写法
	 */
	private String sql;

	private Operator(String op, String sql) {
		this.op = op;
		this.sql = sql;
	}

	public String op() {
		return this.op;
	}

	public String sql() {
		return this.sql;
	}

	public static Operator from(String op) {
		for (Operator operator : values()) {
			if (operator.op().equalsIgnoreCase(op)) {
				return operator;
			}
		}
		return null;
	}

}
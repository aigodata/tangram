package com.github.mengxianun.core.request;

/**
 * 请求 JSON 中 where 的条件运算符
 * 
 * @author mengxiangyun
 *
 */
public enum Operator {

	// 等于
	EQUAL("=", "="),
	// 等于, 强制字符串类型
	STRONG_EQUAL("==", "="),
	// 不等于
	NOT_EQUAL("!=", "<>"),
	// 不等于, 强制字符串类型
	NOT_STRONG_EQUAL("!==", "<>"),
	// 大于
	GT(">", ">"),
	// 大于等于
	GTE(">=", ">="),
	// 小于
	LT("<", "<"),
	// 小于等于
	LTE("<=", "<="),
	// in
	IN(",=", "in", ","),
	// not in
	NOT_IN("!,=", "not in", ","),
	// in
	IN_SQL(",==", "in"),
	// not in
	NOT_IN_SQL("!,==", "not in"),
	// between
	BETWEEN("~=", "between", "~"),
	// not between
	NOT_BETWEEN("!~=", "not between", "~"),
	// like
	LIKE("%=", "like"),
	// not like
	NOT_LIKE("!%=", "not like"),
	// not
	NOT("!", "not"),
	// 空
	NULL("=", "is null"),
	// 非空
	NOT_NULL("!=", "is not null");

	/*
	 * 运算符号
	 */
	private String op;

	/*
	 * 运算符对应的SQL写法
	 */
	private String sql;

	/*
	 * 多值分隔符
	 */
	private String separator;

	private Operator(String op, String sql) {
		this.op = op;
		this.sql = sql;
	}

	private Operator(String op, String sql, String separator) {
		this.op = op;
		this.sql = sql;
		this.separator = separator;
	}

	public String op() {
		return this.op;
	}

	public String sql() {
		return this.sql;
	}

	public String separator() {
		return separator;
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
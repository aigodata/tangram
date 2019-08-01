package com.github.mengxianun.core.schema;
public enum TableType {

	TABLE, VIEW, SYSTEM_TABLE, GLOBAL_TEMPORARY, LOCAL_TEMPORARY, ALIAS, SYNONYM, OTHER;

	public static final TableType[] DEFAULT_TABLE_TYPES = new TableType[] {
			TableType.TABLE, TableType.VIEW };

	public boolean isMaterialized() {
		switch (this) {
		case TABLE:
		case SYSTEM_TABLE:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Tries to resolve a TableType based on an incoming string/literal. If no
	 * fitting TableType is found, OTHER will be returned.
	 */
	public static TableType getTableType(String literalType) {
		literalType = literalType.toUpperCase();
		if ("TABLE".equals(literalType)) {
			return TABLE;
		}
		if ("VIEW".equals(literalType)) {
			return VIEW;
		}
		if ("SYSTEM_TABLE".equals(literalType)) {
			return SYSTEM_TABLE;
		}
		if ("GLOBAL_TEMPORARY".equals(literalType)) {
			return GLOBAL_TEMPORARY;
		}
		if ("LOCAL_TEMPORARY".equals(literalType)) {
			return LOCAL_TEMPORARY;
		}
		if ("ALIAS".equals(literalType)) {
			return ALIAS;
		}
		if ("SYNONYM".equals(literalType)) {
			return SYNONYM;
		}
		return OTHER;
	}
}
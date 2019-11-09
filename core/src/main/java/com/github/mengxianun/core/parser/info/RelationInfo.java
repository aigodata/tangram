package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RelationInfo {

	public static RelationInfo create(String primaryTable, String primaryColumn, String foreignTable,
			String foreignColumn) {
		return new AutoValue_RelationInfo(primaryTable, primaryColumn, foreignTable, foreignColumn);
	}

	public abstract String primaryTable();

	public abstract String primaryColumn();

	public abstract String foreignTable();

	public abstract String foreignColumn();

}

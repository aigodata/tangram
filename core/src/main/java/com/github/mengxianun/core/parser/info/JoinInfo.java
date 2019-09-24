package com.github.mengxianun.core.parser.info;

import com.github.mengxianun.core.request.JoinType;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class JoinInfo {

	public static JoinInfo create(JoinType joinType, TableInfo table) {
		return new AutoValue_JoinInfo(joinType, table);
	}

	public abstract JoinType joinType();

	public abstract TableInfo tableInfo();

}

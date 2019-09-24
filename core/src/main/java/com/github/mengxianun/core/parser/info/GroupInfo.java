package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class GroupInfo {

	public static GroupInfo create(ColumnInfo columnInfo) {
		return new AutoValue_GroupInfo(columnInfo);
	}

	public abstract ColumnInfo columnInfo();

}

package com.github.mengxianun.core.parser.info;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.mengxianun.core.request.Operator;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConditionInfo {

	public static ConditionInfo create(ColumnInfo columnInfo, Operator operator, @Nullable Object value) {
		return create(columnInfo, operator, value, Collections.emptyList());
	}

	public static ConditionInfo create(ColumnInfo columnInfo, Operator operator, @Nullable Object value,
			List<String> relationTablesPath) {
		return new AutoValue_ConditionInfo(columnInfo, operator, value, relationTablesPath);
	}

	public abstract ColumnInfo columnInfo();

	public abstract Operator operator();

	@Nullable
	public abstract Object value();

	public abstract List<String> relationTablesPath();

}

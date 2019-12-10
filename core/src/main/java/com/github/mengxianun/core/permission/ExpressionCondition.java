package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.mengxianun.core.parser.info.RelationInfo;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExpressionCondition implements Condition {

	public static ExpressionCondition create(@Nullable String expression) {
		return create(expression, Collections.emptyList());
	}

	public static ExpressionCondition create(@Nullable String expression, List<RelationInfo> relations) {
		return create(expression, relations, Collections.emptyList());
	}

	public static ExpressionCondition create(@Nullable String expression, List<RelationInfo> relations,
			List<String> relationTablesPath) {
		return new AutoValue_ExpressionCondition(expression, relations, relationTablesPath);
	}

	@Nullable
	public abstract String expression();

	public abstract List<RelationInfo> relations();

	public abstract List<String> relationTablesPath();

}

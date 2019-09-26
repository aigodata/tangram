package com.github.mengxianun.core.permission;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ExpressionCondition implements Condition {

	public static ExpressionCondition create(@Nullable String expression) {
		return new AutoValue_ExpressionCondition(expression);
	}

	@Nullable
	public abstract String expression();

}

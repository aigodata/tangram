package com.github.mengxianun.core.permission;

import java.util.List;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PermissionCheckResult {

	public static PermissionCheckResult create(boolean pass, List<Condition> conditions) {
		return new AutoValue_PermissionCheckResult(pass, conditions);
	}

	public abstract boolean pass();

	public abstract List<Condition> conditions();

}

package com.github.mengxianun.core.permission;

import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class PermissionCheckResult {

	public static PermissionCheckResult create(boolean pass, SimpleInfo simpleInfo) {
		return new AutoValue_PermissionCheckResult(pass, simpleInfo);
	}

	public abstract boolean pass();

	public abstract SimpleInfo simpleInfo();

}

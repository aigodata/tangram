package com.github.mengxianun.core.permission;


public enum PermissionPolicy {

	ALLOW_ALL, STRICT, DENY_ALL;

	public static PermissionPolicy from(String policy) {
		for (PermissionPolicy p : values()) {
			if (p.name().equalsIgnoreCase(policy)) {
				return p;
			}
		}
		return null;
	}

}

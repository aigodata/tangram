package com.github.mengxianun.core.permission;


public enum PermissionPolicy {

	// Allow all
	ALLOW_ALL,
	// Check if permissions are configured for the resource, otherwise fail
	STRICT,
	// Check if permissions are configured for the resource, otherwise pass
	WEAK,
	// Deny all
	DENY_ALL;

	public static PermissionPolicy from(String policy) {
		for (PermissionPolicy p : values()) {
			if (p.name().equalsIgnoreCase(policy)) {
				return p;
			}
		}
		return null;
	}

}

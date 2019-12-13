package com.github.mengxianun.core;

public enum RefreshPolicy {

	// Immediately refresh
	IMMEDIATELY,
	// Refresh in minutes
	MINUTES,
	// Never refresh
	NEVER;

	public static RefreshPolicy from(String policy) {
		for (RefreshPolicy p : values()) {
			if (p.name().equalsIgnoreCase(policy)) {
				return p;
			}
		}
		return null;
	}

}

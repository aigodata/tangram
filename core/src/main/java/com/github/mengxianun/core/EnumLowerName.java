package com.github.mengxianun.core;

/**
 * Enum lowercase name
 * 
 * @author mengxiangyun
 *
 */
public interface EnumLowerName {

	public String name();

	default String lowerName() {
		return name().toLowerCase();
	}

}

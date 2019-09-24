package com.github.mengxianun.core.parser.info;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FileInfo {

	public static FileInfo create(String file) {
		return new AutoValue_FileInfo(file);
	}

	public abstract String file();

}

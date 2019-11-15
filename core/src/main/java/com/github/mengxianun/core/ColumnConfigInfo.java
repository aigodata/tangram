package com.github.mengxianun.core;

import javax.annotation.Nullable;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ColumnConfigInfo {

	public abstract String name();

	@Nullable
	public abstract String timeFormat();

	public static Builder builder() {
		return new AutoValue_ColumnConfigInfo.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder name(String name);

		public abstract Builder timeFormat(String timeFormat);

		public abstract ColumnConfigInfo build();
	}

}

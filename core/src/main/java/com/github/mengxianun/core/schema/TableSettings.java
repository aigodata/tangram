package com.github.mengxianun.core.schema;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TableSettings {

	public static TableSettings defaultSettings() {
		return builder().maxQueryFields(10000).build();
	}

	public abstract int maxQueryFields();

	public static Builder builder() {
		return new AutoValue_TableSettings.Builder();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder maxQueryFields(int value);

		public abstract TableSettings build();
	}

}

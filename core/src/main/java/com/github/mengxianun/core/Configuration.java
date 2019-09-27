package com.github.mengxianun.core;

import javax.annotation.Nullable;

import com.github.mengxianun.core.permission.PermissionPolicy;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Configuration {

	private static final String DEFAULT_CONFIG_FILE = "air.json";
	private static final String DEFAULT_TABLE_CONFIG_PATH = "tables";
	private static final String DEFAULT_ASSOCIATION_CONNECTOR = "__";

	public abstract String configFile();

	public abstract String datasources();

	@Nullable
	public abstract String defaultDatasource();

	public abstract boolean sqlEnabled();

	public abstract boolean nativeEnabled();

	public abstract String tableConfigPath();

	@Nullable
	public abstract String tableAliasExpression();

	public abstract String associationConnector();

	public abstract PermissionPolicy permissionPolicy();

	public static Builder builder() {
		return new AutoValue_Configuration.Builder().configFile(DEFAULT_CONFIG_FILE).sqlEnabled(false)
				.nativeEnabled(false)
				.tableConfigPath(DEFAULT_TABLE_CONFIG_PATH).associationConnector(DEFAULT_ASSOCIATION_CONNECTOR)
				.permissionPolicy(PermissionPolicy.ALLOW_ALL);
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder configFile(String configFile);

		public abstract Builder datasources(String datasources);

		public abstract Builder defaultDatasource(String defaultDatasource);

		public abstract Builder sqlEnabled(boolean sqlEnabled);

		public abstract Builder nativeEnabled(boolean nativeEnabled);

		public abstract Builder tableConfigPath(String tableConfigPath);

		public abstract Builder tableAliasExpression(String tableAliasExpression);

		public abstract Builder associationConnector(String associationConnector);

		public abstract Builder permissionPolicy(PermissionPolicy permissionPolicy);

		public abstract Configuration build();
	}

}

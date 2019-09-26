package com.github.mengxianun.core;

import com.github.mengxianun.core.permission.PermissionPolicy;

@Deprecated
public class ConfigurationOld {

	// 默认配置文件名
	private static final String DEFAULT_CONFIG_FILE = "air.json";
	// 默认数据表配置路径
	private static final String DEFAULT_TABLE_CONFIG_PATH = "tables";
	// 
	private static final String DEFAULT_ASSOCIATION_CONNECTOR = "__";

	private String configFile = DEFAULT_CONFIG_FILE;
	private String datasources;
	private boolean upsert;
	private boolean nativeEnabled;
	private String defaultDatasource;
	private String tableConfigPath = DEFAULT_TABLE_CONFIG_PATH;
	private String tableAliasExpression;
	private String associationConnector = DEFAULT_ASSOCIATION_CONNECTOR;
	private PermissionPolicy permissionPolicy;

	public ConfigurationOld(Builder builder) {
		this.datasources = builder.datasources;
		this.upsert = builder.upsert;
		this.nativeEnabled = builder.nativeEnabled;
		this.defaultDatasource = builder.defaultDatasource;
		if (builder.tableConfigPath != null) {
			this.tableConfigPath = builder.tableConfigPath;
		}
		this.tableAliasExpression = builder.tableAliasExpression;
		if (builder.associationConnector != null) {
			this.associationConnector = builder.associationConnector;
		}
		this.permissionPolicy = builder.permissionPolicy;
	}

	public String getConfigFile() {
		return configFile;
	}

	public String getDatasources() {
		return datasources;
	}

	public boolean isUpsert() {
		return upsert;
	}

	public boolean isNativeEnabled() {
		return nativeEnabled;
	}

	public String getDefaultDatasource() {
		return defaultDatasource;
	}

	public String getTableConfigPath() {
		return tableConfigPath;
	}

	public String getTableAliasExpression() {
		return tableAliasExpression;
	}

	public String getAssociationConnector() {
		return associationConnector;
	}

	public PermissionPolicy getPermissionPolicy() {
		return permissionPolicy;
	}

	static Configuration fromConfig(String configFile) {
		throw new UnsupportedOperationException();
	}

	static Builder builder() {
		return ConfigurationOld.builder();
	}

	public static class Builder {

		private String datasources;
		private boolean upsert;
		private boolean nativeEnabled;
		private String defaultDatasource;
		private String tableConfigPath;
		private String tableAliasExpression;
		private String associationConnector;
		private PermissionPolicy permissionPolicy;

		public Builder setDatasources(String datasources) {
			this.datasources = datasources;
			return this;
		}

		public Builder setUpsert(boolean upsert) {
			this.upsert = upsert;
			return this;
		}

		public Builder setNativeEnabled(boolean nativeEnabled) {
			this.nativeEnabled = nativeEnabled;
			return this;
		}

		public Builder setDefaultDatasource(String defaultDatasource) {
			this.defaultDatasource = defaultDatasource;
			return this;
		}

		public Builder setTableConfigPath(String tableConfigPath) {
			this.tableConfigPath = tableConfigPath;
			return this;
		}

		public Builder setTableAliasExpression(String tableAliasExpression) {
			this.tableAliasExpression = tableAliasExpression;
			return this;
		}

		public Builder setAssociationConnector(String associationConnector) {
			this.associationConnector = associationConnector;
			return this;
		}

		public Builder setPermissionPolicy(PermissionPolicy permissionPolicy) {
			this.permissionPolicy = permissionPolicy;
			return this;
		}

		public ConfigurationOld build() {
			return new ConfigurationOld(this);
		}
	}

}

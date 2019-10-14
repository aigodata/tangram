package com.github.mengxianun.elasticsearch.attributes;

public final class ElasticsearchDatasourceConfig {

	private ElasticsearchDatasourceConfig() {
		throw new AssertionError();
	}

	public static final int DEFAULT_HTTP_PORT = 9200;

	public static final String URL = "url";
	public static final String HTTP_PORT = "httpPort";

}

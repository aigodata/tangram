package com.github.mengxianun.elasticsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import com.alibaba.druid.pool.ElasticSearchDruidDataSourceFactory;
import com.github.mengxianun.core.DataContextFactory;
import com.github.mengxianun.elasticsearch.attributes.ElasticsearchDatasourceAttributes;
import com.google.auto.service.AutoService;
import com.google.gson.JsonObject;

@AutoService(DataContextFactory.class)
public class ElasticsearchDataContextFactory implements DataContextFactory {

	@Override
	public String getType() {
		return "elasticsearch";
	}

	@Override
	public ElasticsearchDataContext create(JsonObject dataSourceJsonObject) {
		String url = dataSourceJsonObject.get(ElasticsearchDatasourceAttributes.URL).getAsString();
		int httpPort = dataSourceJsonObject.has(ElasticsearchDatasourceAttributes.HTTP_PORT)
				? dataSourceJsonObject.get(ElasticsearchDatasourceAttributes.HTTP_PORT).getAsInt()
				: ElasticsearchDatasourceAttributes.DEFAULT_HTTP_PORT;
		List<HttpHost> httpHosts = createElasticsearchHttpHost(url, httpPort);
		RestClient client = RestClient.builder(httpHosts.toArray(new HttpHost[] {})).build();

		Properties properties = new Properties();
		properties.put(ElasticsearchDatasourceAttributes.URL, url);
		DataSource dataSource = null;
		try {
			dataSource = ElasticSearchDruidDataSourceFactory.createDataSource(properties);
		} catch (Exception e) {
			throw new ElasticsearchDataException("Failed to create data source");
		}
		return new ElasticsearchDataContext(dataSource, client);
	}

	private List<HttpHost> createElasticsearchHttpHost(String url, int httpPort) {
		List<HttpHost> httpHosts = new ArrayList<>();
		String pattern = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?";
		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url);
		while (matcher.find()) {
			String[] ipPort = matcher.group().split(":");
			String ip = ipPort[0];
			HttpHost httpHost = new HttpHost(ip, httpPort);
			httpHosts.add(httpHost);
		}
		return httpHosts;
	}

}

package com.github.mengxianun.elasticsearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.DataContextFactory;
import com.github.mengxianun.elasticsearch.attributes.ElasticsearchDatasourceConfig;
import com.google.auto.service.AutoService;
import com.google.gson.JsonObject;

@AutoService(DataContextFactory.class)
public class ElasticsearchDataContextFactory implements DataContextFactory {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDataContextFactory.class);

	@Override
	public String getType() {
		return "elasticsearch";
	}

	@Override
	public ElasticsearchDataContext create(JsonObject dataSourceJsonObject) {
		String multiUrl = dataSourceJsonObject.get(ElasticsearchDatasourceConfig.URL).getAsString();
		List<HttpHost> httpHosts = createHttpHost(multiUrl);
		return new ElasticsearchDataContext(httpHosts.stream().toArray(HttpHost[]::new));
	}

	private List<HttpHost> createHttpHost(String multiUrl) {
		List<HttpHost> httpHosts = new ArrayList<>();
		String[] urls = multiUrl.split(",");
		for (String urlString : urls) {
			URL url;
			try {
				url = new URL(urlString);
				httpHosts.add(new HttpHost(url.getHost(), url.getPort()));
			} catch (MalformedURLException e) {
				logger.error("URL parsing failed", e);
			}
		}
		return httpHosts;
	}

}

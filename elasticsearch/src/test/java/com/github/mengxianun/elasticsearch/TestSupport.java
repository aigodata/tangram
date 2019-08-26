package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeAll;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.DefaultTranslator;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TestSupport {

	static final Logger LOG = Logger.getLogger(TestSupport.class.getName());
	
	private static final String TEST_INDEX = "test";
	private static final String TEST_DATA = "index.json";
	private static final String TEST_CONFIG_FILE = "test.json";
	public static final DefaultTranslator translator;

	static {
		translator = new DefaultTranslator(TEST_CONFIG_FILE);
		translator.addFactory(new ElasticsearchDataContextFactory());
		translator.reInit();
	}

	@BeforeAll
	public static void init() {
		// Initialize test data
		RestClientBuilder clientBuilder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
		try (RestHighLevelClient client = new RestHighLevelClient(clientBuilder)) {
			IndicesClient indices = client.indices();
			// Delete
			GetIndexRequest getIndexRequest = new GetIndexRequest(TEST_INDEX);
			boolean exists = indices.exists(getIndexRequest, RequestOptions.DEFAULT);
			if (exists) {
				DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(TEST_INDEX);
				indices.delete(deleteIndexRequest, RequestOptions.DEFAULT);
			}
			// Create
			CreateIndexRequest createIndexRequest = new CreateIndexRequest(TEST_INDEX);
			indices.create(createIndexRequest, RequestOptions.DEFAULT);

			String dataString = Resources.toString(Resources.getResource(TEST_DATA), StandardCharsets.UTF_8);
			JsonArray dataArray = new Gson().fromJson(dataString, JsonArray.class);
			BulkRequest bulkRequest = new BulkRequest();
			for (JsonElement jsonElement : dataArray) {
				String source = jsonElement.getAsJsonObject().toString();
				IndexRequest indexRequest = new IndexRequest(TEST_INDEX, "_doc");
				indexRequest.source(source, XContentType.JSON);
				bulkRequest.add(indexRequest);
			}
			bulkRequest.setRefreshPolicy(RefreshPolicy.IMMEDIATE);
			BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
			int length = bulkResponse.getItems().length;

			LOG.info("Initialize " + length + " elasticsearch test data");
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Elasticsearch create index failed", e);
		}

	}

	String readJson(String jsonFile) {
		URL url = Resources.getResource(jsonFile);
		try {
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOG.warning(e.getMessage());
			return "";
		}
	}

	DataResultSet run(String jsonFile) {
		return runJson(readJson(jsonFile));
	}

	DataResultSet runJson(String json) {
		DataResultSet dataResultSet = translator.translate(json);
		LOG.info("-----------------Json-----------------");
		LOG.info(json);
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		LOG.info("-----------------Result-----------------");
		LOG.info(gson.toJson(dataResultSet.getData()));
		return dataResultSet;
	}

}

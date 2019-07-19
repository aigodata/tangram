package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.resutset.DefaultDataResult;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticsearchDataContext extends JdbcDataContext {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDataContext.class);
	// Elasticsearch 没有Schema, 这里定义一个虚拟的Schema, 用于程序调用
	private static final String VIRTUAL_SCHEMA = "elasticsearch";

	private final RestClient client;

	public ElasticsearchDataContext(DataSource dataSource, RestClient client) {
		super(dataSource);
		this.client = client;
	}

	@Override
	public void initMetadata() {
		JsonObject allMappings = null;
		try {
			// 查询所有索引的mapping
			Response response = client.performRequest("GET", "/_all/_mapping");
			String responseBody = EntityUtils.toString(response.getEntity());
			allMappings = new JsonParser().parse(responseBody).getAsJsonObject();
		} catch (IOException e) {
			logger.error("Elasticsearch index mapping failed to read", e);
			return;
		}
		List<Schema> schemas = new ArrayList<>();
		DefaultSchema schema = new DefaultSchema(VIRTUAL_SCHEMA);
		schemas.add(schema);
		metadata.setSchemas(schemas);
		for (String index : allMappings.keySet()) {
			DefaultTable table = new DefaultTable(index);
			schema.addTable(table);
			JsonObject indexConfig = allMappings.getAsJsonObject(index);
			JsonObject mappings = indexConfig.getAsJsonObject("mappings");
			if (mappings.isJsonNull() || mappings.size() == 0) {
				continue;
			}
			String type = mappings.keySet().iterator().next();
			JsonObject typeObject = mappings.getAsJsonObject(type);
			JsonObject columns = typeObject.getAsJsonObject("properties");
			for (String columnName : columns.keySet()) {
				DefaultColumn column = new DefaultColumn(table, columnName);
				table.addColumn(column);
			}
		}
	}

	@Override
	public DataResult executeNative(String statement) {
		String responseBody = null;
		try (NStringEntity nStringEntity = new NStringEntity(statement, ContentType.APPLICATION_JSON)) {
			Response response = client.performRequest("GET", "/_search", new HashMap<>(), nStringEntity);
			responseBody = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			logger.error(ResultStatus.NATIVE_FAILED.message(), e);
			throw new ElasticsearchDataException(ResultStatus.NATIVE_FAILED);
		}
		JsonObject responseObject = new JsonParser().parse(responseBody).getAsJsonObject();
		return new DefaultDataResult(responseObject);
	}

	@Override
	public void destroy() {
		try {
			client.close();
		} catch (IOException e) {
			logger.error(ResultStatus.RESOURCE_DESTROY_FAILED.fill("Elasticsearch client"), e);
		}
	}

}

package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.elasticsearch.dialect.ElasticsearchDialect;
import com.github.mengxianun.elasticsearch.processor.ElasticsearchRowProcessor;
import com.github.mengxianun.jdbc.JdbcDataContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ElasticsearchDataContext extends JdbcDataContext {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDataContext.class);
	// Elasticsearch 没有Schema, 这里定义一个虚拟的Schema, 用于程序调用
	private static final String VIRTUAL_SCHEMA = "elasticsearch";

	private final RestClient client;

	public ElasticsearchDataContext(DataSource dataSource, RestClient client) {
		if (dataSource == null || client == null) {
			throw new IllegalArgumentException("DataSource and RestClient cannot be null");
		}
		this.dataSource = dataSource;
		this.dialect = new ElasticsearchDialect();
		this.runner = new QueryRunner(dataSource);
		this.convert = new ElasticsearchRowProcessor();
		closeConnection.set(true);
		this.client = client;
		initializeMetadata();
	}

	@Override
	public void initializeMetadata() {
		JsonObject allMappings = null;
		try {
			// 查询所有索引的mapping
			Response response = client.performRequest("GET", "/_all/_mapping");
			String responseBody = EntityUtils.toString(response.getEntity());
			allMappings = new JsonParser().parse(responseBody).getAsJsonObject();
		} catch (IOException e) {
			logger.error(String.format("Elasticsearch index mapping failed to read"), e);
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
				DefaultColumn column = new DefaultColumn(columnName, table);
				table.addColumn(column);
			}
		}
	}

	@Override
	public JsonElement executeNative(Table table, String script) {
		String responseBody = null;
		try {
			NStringEntity nStringEntity = new NStringEntity(script, ContentType.APPLICATION_JSON);
			Response response = client.performRequest("GET", "/" + table + "/_search", new HashMap<>(), nStringEntity);
			responseBody = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			logger.error(ResultStatus.NATIVE_FAILED.message(), e);
			throw new ElasticsearchDataException(ResultStatus.NATIVE_FAILED);
		}
		JsonObject responseObject = new JsonParser().parse(responseBody).getAsJsonObject();
		return responseObject;
	}

	@Override
	public void destroy() throws IOException {
		client.close();
	}

}

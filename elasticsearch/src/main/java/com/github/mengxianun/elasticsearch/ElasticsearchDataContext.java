package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.AbstractDataContext;
import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.Atom;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.SQLBuilder;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.data.DataSet;
import com.github.mengxianun.core.data.update.UpdateSummary;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.resutset.DefaultDataResult;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.TableType;
import com.github.mengxianun.elasticsearch.dialect.ElasticsearchDialect;
import com.github.mengxianun.elasticsearch.schema.ElasticsearchColumnType;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElasticsearchDataContext extends AbstractDataContext {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDataContext.class);
	// Elasticsearch 没有Schema, 这里定义一个虚拟的Schema, 用于程序调用
	private static final String VIRTUAL_SCHEMA = "elasticsearch";
	private static final String REQUEST_METHOD_GET = "GET";
	private static final String REQUEST_ENDPOINT_ROOT = "/";
	private static final String REQUEST_ENDPOINT_MAPPING = "/_mapping";

	private final RestHighLevelClient client;

	private final String version;

	public ElasticsearchDataContext(HttpHost... httpHosts) {
		this(new RestHighLevelClient(RestClient.builder(httpHosts)));
	}

	public ElasticsearchDataContext(RestHighLevelClient client) {
		this.client = client;
		this.version = readVersion();
		dialect = new ElasticsearchDialect();
		initMetadata();
	}

	private String readVersion() {
		Request infoRequest = new Request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_ROOT);
		String infoString = request(infoRequest);
		JsonObject infoObject = new Gson().fromJson(infoString, JsonObject.class);
		return infoObject.getAsJsonObject("version").get("number").getAsString();
	}

	@Override
	public void initMetadata() {
		DefaultSchema schema = new DefaultSchema(VIRTUAL_SCHEMA);
		metadata.setSchemas(Lists.newArrayList(schema));

		Request mappingRequest = new Request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_MAPPING);
		String mappingString = request(mappingRequest);
		JsonObject mappingObject = new Gson().fromJson(mappingString, JsonObject.class);
		for (Entry<String, JsonElement> entry : mappingObject.entrySet()) {
			String index = entry.getKey();
			JsonObject mappingMetaData = entry.getValue().getAsJsonObject();
			DefaultTable table = new DefaultTable(index, TableType.TABLE, schema);
			schema.addTable(table);

			JsonObject mappingNode = mappingMetaData.getAsJsonObject("mappings");
			if (mappingNode.size() == 0) {
				continue;
			}
			JsonObject properties = mappingNode.getAsJsonObject(mappingNode.keySet().iterator().next())
					.getAsJsonObject("properties");

			for (String columnName : properties.keySet()) {
				JsonObject columnProperties = properties.getAsJsonObject(columnName);
				String typeName = columnProperties.has("type") ? columnProperties.get("type").getAsString() : null;
				DefaultColumn column = new DefaultColumn(table, new ElasticsearchColumnType(typeName), columnName);
				table.addColumn(column);
			}
		}
	}

	@Override
	public SQLBuilder getSQLBuilder(Action action) {
		return new ElasticsearchSQLBuilder(action);
	}

	@Override
	protected void trans(Atom... atoms) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected DataSet select(String sql, Object... params) {
		String resultString = run(sql, params);
		return new ElasticsearchSQLDataSet(resultString);
	}

	@Override
	protected UpdateSummary insert(String sql, Object... params) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected UpdateSummary update(String sql, Object... params) {
		throw new UnsupportedOperationException();
	}

	private String run(String sql, Object... params) {
		String fullSql = null;
		try {
			fullSql = SQLParser.fill(sql, params);
		} catch (SQLException e) {
			throw new ElasticsearchException("Elasticsearch operation failed", e);
		}
		Request request = new Request("POST", "/_xpack/sql");
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("query", fullSql);
		request.setJsonEntity(jsonObject.toString());
		try {
			Response response = client.getLowLevelClient().performRequest(request);
			return EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			throw new ElasticsearchException("Elasticsearch operation failed", e);
		}
	}

	@Override
	public DataResult executeNative(Operation operation, String resource, String statement) {
		if (operation != Operation.SELECT) {
			throw new UnsupportedOperationException();
		}
		String method = "GET";
		String endpoint = resource;
		Request request = new Request(method, endpoint);
		request.setJsonEntity(statement);
		String resultString = null;
		try {
			Response response = client.getLowLevelClient().performRequest(request);
			resultString = EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			throw new ElasticsearchException("Native statement execution failed", e);
		}
		return new DefaultDataResult(resultString);
	}

	public String request(Request request) {
		try {
			Response response = client.getLowLevelClient().performRequest(request);
			return EntityUtils.toString(response.getEntity());
		} catch (ParseException | IOException e) {
			throw new ElasticsearchException("Elasticsearch Request failed", e);
		}
	}

	@Override
	public void destroy() {
		try {
			client.close();
		} catch (IOException e) {
			logger.error(ResultStatus.RESOURCE_DESTROY_FAILED.fill("Elasticsearch client"), e);
		}
	}

	public String getVersion() {
		return version;
	}

}

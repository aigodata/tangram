package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
import com.github.mengxianun.core.data.DefaultDataSetHeader;
import com.github.mengxianun.core.data.update.UpdateSummary;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.resutset.DefaultDataResult;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.TableType;
import com.github.mengxianun.elasticsearch.data.ElasticsearchDataSet;
import com.github.mengxianun.elasticsearch.data.ElasticsearchSQLDataSet;
import com.github.mengxianun.elasticsearch.dialect.ElasticsearchDialect;
import com.github.mengxianun.elasticsearch.schema.ElasticsearchColumnType;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElasticsearchDataContext extends AbstractDataContext {

	private static final Logger logger = LoggerFactory.getLogger(ElasticsearchDataContext.class);
	// Elasticsearch 没有Schema, 这里定义一个虚拟的Schema, 用于程序调用
	private static final String VIRTUAL_SCHEMA = "elasticsearch";
	private static final String REQUEST_METHOD_GET = "GET";
	private static final String REQUEST_METHOD_POST = "POST";
	private static final String REQUEST_ENDPOINT_ROOT = "/";
	private static final String REQUEST_ENDPOINT_MAPPING = "/_mapping";
	private static final String REQUEST_ENDPOINT_SEARCH = "/_search";
	private static final String REQUEST_ENDPOINT_SQL = "/_xpack/sql";
	private static final String REQUEST_ENDPOINT_SQL_TRANSLATE = "/_xpack/sql/translate";

	private static final String LIMIT_FROM = "from";
	private static final String LIMIT_SIZE = "size";

	private final RestHighLevelClient client;

	private final String version;

	public ElasticsearchDataContext(HttpHost... httpHosts) {
		this(new RestHighLevelClient(RestClient.builder(httpHosts)));
	}

	public ElasticsearchDataContext(RestHighLevelClient client) {
		this.client = client;
		this.version = readVersion();
		logger.info("Elasticsearch version: {}", version);
		dialect = new ElasticsearchDialect();
		initMetadata();
	}

	private String readVersion() {
		String infoString = request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_ROOT);
		JsonObject infoObject = new Gson().fromJson(infoString, JsonObject.class);
		return infoObject.getAsJsonObject("version").get("number").getAsString();
	}

	@Override
	public void initMetadata() {
		DefaultSchema schema = new DefaultSchema(VIRTUAL_SCHEMA);
		metadata.setSchemas(Lists.newArrayList(schema));

		String mappingString = request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_MAPPING);
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
	protected Object query(Action action) {
		// 说明: 6.8.2 以及之前的版本, SQL分页只支持 LIMIT, 不支持 OFFSET
		// 所有在这里, 不分页的查询通过 Elasticsearch SQL 的方式实现
		// 分页查询通过将 SQL translate, 再进行查询
		if (!action.isLimit()) {
			return super.query(action);
		}

		Gson gson = new Gson();
		// translate

		// Build query
		ElasticsearchSQLBuilder sqlBuilder = new ElasticsearchSQLBuilder(action);
		sqlBuilder.toSelectWithoutLimit();
		String sql = sqlBuilder.getSql();
		Object[] params = sqlBuilder.getParams().toArray();
		String fullSql = fill(sql, params);
		String nativeQueryString = translateSQL(fullSql);
		JsonObject query = gson.fromJson(nativeQueryString, JsonObject.class);

		// aggregations/limit
		LimitItem limitItem = action.getLimitItem();
		long from = limitItem.getStart();
		long size = limitItem.getLimit();
		if (action.isGroup()) {
			processAggrQuery(query, from, size);
		} else {
			query.addProperty(LIMIT_FROM, from);
			query.addProperty(LIMIT_SIZE, size);
		}
		// Request
		TableItem tableItem = action.getTableItems().get(0);
		Table table = tableItem.getTable();
		String index = table != null ? table.getName() : tableItem.getExpression();
		String endpoint = REQUEST_ENDPOINT_ROOT + index + REQUEST_ENDPOINT_SEARCH;
		String statement = query.toString();
		String resultString = request(REQUEST_METHOD_GET, endpoint, statement);
		// Response
		JsonObject response = gson.fromJson(resultString, JsonObject.class);
		JsonObject hits = response.getAsJsonObject("hits");
		long total = hits.get("total").getAsLong();
		ElasticsearchDataSet dataSet = new ElasticsearchDataSet(new DefaultDataSetHeader(action.getColumnItems()),
				resultString);
		Object result = render(dataSet.toRows(), action);
		if (action.isLimit()) {
			result = pageResult(limitItem.getStart(), limitItem.getEnd(), total, result);
		}
		return result;
	}

	private void processAggrQuery(JsonObject query, long from, long size) {
		JsonObject aggrObject = query.getAsJsonObject("aggregations");
		JsonObject groupbyObject = aggrObject.getAsJsonObject("groupby");
		JsonArray sources = groupbyObject.getAsJsonObject("composite").getAsJsonArray("sources");
		List<JsonElement> autoNameSources = new ArrayList<>();
		List<JsonElement> newNameSources = new ArrayList<>();
		sources.forEach(source -> {
			JsonObject sourceObject = source.getAsJsonObject();
			String groupKey = sourceObject.keySet().iterator().next();
			JsonObject groupObject = sourceObject.getAsJsonObject(groupKey);
			JsonObject fieldObject = groupObject.entrySet().iterator().next().getValue().getAsJsonObject();
			String field = fieldObject.get("field").getAsString();

			JsonObject newSource = new JsonObject();
			newSource.add(field, groupObject);
			newNameSources.add(newSource);

			autoNameSources.add(source);
		});
		autoNameSources.forEach(sources::remove);
		newNameSources.forEach(sources::add);
		// 添加聚合分页 Bucket Sort
		JsonObject bucketSortObject = new JsonObject();
		bucketSortObject.addProperty("from", from);
		bucketSortObject.addProperty("size", size);

		JsonObject bucketTruncateObject = new JsonObject();
		bucketTruncateObject.add("bucket_sort", bucketSortObject);

		JsonObject aggsObject = new JsonObject();
		aggsObject.add("bucket_truncate", bucketTruncateObject);
		groupbyObject.add("aggs", aggsObject);
	}

	@Override
	protected DataSet select(String sql, Object... params) {
		String resultString = runSQL(sql, params);
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

	private String fill(String sql, Object... params) {
		try {
			return SQLParser.fill(sql, params);
		} catch (SQLException e) {
			throw new ElasticsearchException("Elasticsearch SQL fill failed", e);
		}
	}

	private String runSQL(String sql, Object... params) {
		String fullSql = fill(sql, params);
		JsonObject query = new JsonObject();
		query.addProperty("query", fullSql);
		return request(REQUEST_METHOD_POST, REQUEST_ENDPOINT_SQL, query.toString());
	}

	private String translateSQL(String sql) {
		JsonObject query = new JsonObject();
		query.addProperty("query", sql);
		return request(REQUEST_METHOD_POST, REQUEST_ENDPOINT_SQL_TRANSLATE, query.toString());
	}

	@Override
	public DataResult executeNative(Operation operation, String resource, String statement) {
		if (operation != Operation.SELECT) {
			throw new UnsupportedOperationException();
		}
		String resultString = request(REQUEST_METHOD_GET, resource, statement);
		return new DefaultDataResult(resultString);
	}

	private String request(String method, String endpoint) {
		return request(method, endpoint, "");
	}

	private String request(String method, String endpoint, String statement) {
		logger.debug("Elasticsearch Request: {} {} {}", method, endpoint, statement);
		Request request = new Request(method, endpoint);
		request.setJsonEntity(statement);
		return request(request);
	}

	private String request(Request request) {
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

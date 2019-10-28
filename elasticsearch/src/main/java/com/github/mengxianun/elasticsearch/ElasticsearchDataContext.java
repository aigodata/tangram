package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.AbstractDataContext;
import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.Atom;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.SQLBuilder;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.BasicSummary;
import com.github.mengxianun.core.data.summary.InsertSummary;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.github.mengxianun.core.data.summary.UpdateSummary;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.TableType;
import com.github.mengxianun.elasticsearch.data.ElasticsearchQuerySummary;
import com.github.mengxianun.elasticsearch.data.ElasticsearchSQLQuerySummary;
import com.github.mengxianun.elasticsearch.dialect.ElasticsearchDialect;
import com.github.mengxianun.elasticsearch.schema.ElasticsearchColumnType;
import com.github.mengxianun.elasticsearch.schema.ElasticsearchTable;
import com.google.common.base.Strings;
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
	private static final String REQUEST_ENDPOINT_ALIAS = "/_alias";
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
		JsonObject infoObject = App.gson().fromJson(infoString, JsonObject.class);
		return infoObject.getAsJsonObject("version").get("number").getAsString();
	}

	@Override
	public void initMetadata() {
		schema = loadSchema();
	}
	
	@Override
	public Schema loadSchema() {
		Schema elasticSchema = new DefaultSchema(VIRTUAL_SCHEMA);
		// mapping
		String mappingString = request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_MAPPING);
		JsonObject mappingObject = App.gson().fromJson(mappingString, JsonObject.class);
		int indexNum = mappingObject.keySet().size();
		if (indexNum <= 0) {
			return elasticSchema;
		}
		for (Entry<String, JsonElement> entry : mappingObject.entrySet()) {
			String index = entry.getKey();
			JsonObject mappingMetaData = entry.getValue().getAsJsonObject();
			ElasticsearchTable table = createTable(elasticSchema, index, mappingMetaData);
			elasticSchema.addTable(table);
			logger.info("Find elasticsearch index [{}]", index);
		}

		// alias
		String aliasString = request(REQUEST_METHOD_GET, REQUEST_ENDPOINT_ALIAS);
		JsonObject aliasObject = App.gson().fromJson(aliasString, JsonObject.class);
		for (Entry<String, JsonElement> entry : aliasObject.entrySet()) {
			String index = entry.getKey();
			Set<String> aliases = entry.getValue().getAsJsonObject().getAsJsonObject("aliases").keySet();
			ElasticsearchTable table = (ElasticsearchTable) elasticSchema.getTableByName(index);
			table.addAliases(aliases);

			logger.info("Find elasticsearch index [{}] alias {}", index, aliases);
		}
		return elasticSchema;
	}

	@Override
	public Table loadTable(String name) {
		try {
			return loadIndex(name);
		} catch (Exception e) {
			logger.error("Load table [{}].[{}] failed.", schema.getName(), name);
			return null;
		}
	}

	public Table loadIndex(String tableName) {
		if (Strings.isNullOrEmpty(tableName)) {
			return null;
		}
		ElasticsearchTable table = null;

		String mappingString = request(REQUEST_METHOD_GET, tableName + REQUEST_ENDPOINT_MAPPING);
		JsonObject mappingObject = App.gson().fromJson(mappingString, JsonObject.class);
		int indexNum = mappingObject.keySet().size();
		if (indexNum <= 0) {
			return table;
		}
		Entry<String, JsonElement> indexEntry = mappingObject.entrySet().iterator().next();
		JsonObject mappingMetaData = indexEntry.getValue().getAsJsonObject();
		table = createTable(schema, tableName, mappingMetaData);
		logger.info("Find elasticsearch index [{}]", tableName);

		// alias
		String aliasString = request(REQUEST_METHOD_GET, tableName + REQUEST_ENDPOINT_ALIAS);
		JsonObject aliasObject = App.gson().fromJson(aliasString, JsonObject.class);
		Entry<String, JsonElement> aliasEntry = aliasObject.entrySet().iterator().next();
		Set<String> aliases = aliasEntry.getValue().getAsJsonObject().getAsJsonObject("aliases").keySet();
		table.addAliases(aliases);
		logger.info("Find elasticsearch index [{}] alias {}", tableName, aliases);

		return table;
	}

	private ElasticsearchTable createTable(Schema schema, String index, JsonObject mappingMetaData) {
		ElasticsearchTable table = new ElasticsearchTable(index, TableType.TABLE, schema);

		JsonObject mappingNode = mappingMetaData.getAsJsonObject("mappings");
		if (mappingNode.size() == 0) {
			return table;
		}
		JsonObject defaultTypeMapping = mappingNode.getAsJsonObject(mappingNode.keySet().iterator().next());
		if (defaultTypeMapping.has("properties")) {
			JsonObject properties = defaultTypeMapping.getAsJsonObject("properties");
			for (String columnName : properties.keySet()) {
				JsonObject columnProperties = properties.getAsJsonObject(columnName);
				if (columnProperties.has("properties")) { // object type
					String typeName = ElasticsearchColumnType.OBJECT;
					List<List<String>> paths = new ArrayList<>();
					findAllColumns(Arrays.asList(columnName), paths, columnProperties);
					for (List<String> list : paths) {
						String objectColumnName = String.join(".", list);
						if ("query.match_all".equals(objectColumnName)) { // skip built-in column
							continue;
						}
						DefaultColumn column = new DefaultColumn(objectColumnName,
								new ElasticsearchColumnType(typeName), table);
						table.addColumn(column);
					}
				} else {
					String typeName = columnProperties.has("type") ? columnProperties.get("type").getAsString()
							: null;
					DefaultColumn column = new DefaultColumn(columnName,
							new ElasticsearchColumnType(typeName), table);
					table.addColumn(column);
				}
			}
		}
		return table;
	}

	private void findAllColumns(List<String> visited, List<List<String>> paths, JsonObject columnObjects) {
		if (!columnObjects.has("properties")) {
			paths.add(visited);
			return;
		}
		JsonObject columns = columnObjects.getAsJsonObject("properties");
		for (Entry<String, JsonElement> entry : columns.entrySet()) {
			String column = entry.getKey();
			JsonObject columnObject = entry.getValue().getAsJsonObject();
			List<String> temp = new ArrayList<>();
			temp.addAll(visited);
			temp.add(column);
			findAllColumns(temp, paths, columnObject);
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
	protected QuerySummary select(Action action) {
		// Build query
		ElasticsearchSQLBuilder sqlBuilder = new ElasticsearchSQLBuilder(action);
		sqlBuilder.toSelectWithoutLimit();
		String sql = sqlBuilder.getSql();
		Object[] params = sqlBuilder.getParams().toArray();
		if (action.isLimit()) {
			// 说明: 6.8.2 以及之前的版本, SQL分页只支持 LIMIT, 不支持 OFFSET
			// 所有在这里, 分页查询通过将 SQL translate, 再进行查询

			String fullSql = fill(sql, params);
			// translate
			String nativeQueryString = translateSQL(fullSql);
			JsonObject query = App.gson().fromJson(nativeQueryString, JsonObject.class);

			// limit
			LimitItem limitItem = action.getLimitItem();
			long from = limitItem.getStart();
			long size = limitItem.getLimit();
			// aggregations
			if (action.isGroup()) {
				processAggrQuery(query);
				processAggrLimit(query, from, size);
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
			return new ElasticsearchQuerySummary(action, resultString);
		} else {
			String resultString = runSQL(sql, params);
			return new ElasticsearchSQLQuerySummary(action, resultString);
		}
	}

	@Override
	protected InsertSummary insert(Action action) {
		return insert(fill(action.getSql(), action.getParams().toArray()));
	}

	@Override
	protected UpdateSummary update(Action action) {
		return update(fill(action.getSql(), action.getParams().toArray()));
	}

	@Override
	protected QuerySummary select(String sql) {
		String resultString = runSQL(sql);
		return new ElasticsearchSQLQuerySummary(resultString);
	}

	@Override
	protected InsertSummary insert(String sql) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected UpdateSummary update(String sql) {
		throw new UnsupportedOperationException();
	}

	private void processAggrQuery(JsonObject query) {
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
	}

	private void processAggrLimit(JsonObject query, long from, long size) {
		JsonObject aggrObject = query.getAsJsonObject("aggregations");
		JsonObject groupbyObject = aggrObject.getAsJsonObject("groupby");
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
	public Summary executeNative(String statement) {
		JsonObject nativeObject = App.gson().fromJson(statement, JsonObject.class);
		String endpoint = nativeObject.get("endpoint").getAsString();
		JsonObject bodyObject = nativeObject.getAsJsonObject("body");
		String resultString = request(REQUEST_METHOD_GET, endpoint, bodyObject.toString());
		return new BasicSummary(App.gson().fromJson(resultString, JsonObject.class));
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
		} catch (ResponseException e) {
			String message = e.getMessage();
			Response response = e.getResponse();
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					message = EntityUtils.toString(entity);
				} catch (ParseException | IOException e1) {
					throw new ElasticsearchException("Elasticsearch Request failed", e1);
				}
			}
			throw new ElasticsearchException(message);
		} catch (IOException e) {
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

package com.github.mengxianun.elasticsearch.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class ElasticsearchQuerySummary extends QuerySummary {

	private static final String NODE_TOTAL = "total";
	private static final String NODE_HITS = "hits";
	private static final String NODE_AGGREGATIONS = "aggregations";
	private static final String NODE_GROUPBY = "groupby";
	private static final String NODE_BUCKETS = "buckets";
	private static final String NODE_SOURCE = "_source";
	private static final String NODE_FIELDS = "fields";
	private static final String NODE_KEY = "key";

	private final String resultString;

	public ElasticsearchQuerySummary(String resultString) {
		this(null, resultString);
	}

	public ElasticsearchQuerySummary(Action action, String resultString) {
		super(action, null);
		this.resultString = resultString;
	}

	@Override
	public List<Row> toRows() {
		List<Row> rows = new ArrayList<>();
		JsonObject response = App.gson().fromJson(resultString, JsonObject.class);

		// hits
		JsonObject hits = response.getAsJsonObject(NODE_HITS);
		total = hits.get(NODE_TOTAL).getAsLong();
		JsonArray items = hits.getAsJsonArray(NODE_HITS);
		if (items.size() > 0) {
			items.forEach(e -> rows.add(new DefaultRow(header, parseHit(e.getAsJsonObject()))));
		}

		// aggregations
		if (response.has(NODE_AGGREGATIONS)) {
			JsonObject aggregations = response.getAsJsonObject(NODE_AGGREGATIONS);
			JsonObject groupby = aggregations.getAsJsonObject(NODE_GROUPBY);
			JsonArray buckets = groupby.get(NODE_BUCKETS).getAsJsonArray();
			buckets.forEach(e -> rows.add(new DefaultRow(header, parseBucket(e.getAsJsonObject()))));
		}
		return rows;
	}

	private Object[] parseHit(JsonObject hit) {
		final Object[] values = new Object[header.size()];
		// _source
		if (hit.has(NODE_SOURCE)) {
			JsonObject source = hit.getAsJsonObject(NODE_SOURCE);
			for (Entry<String, JsonElement> entry : source.entrySet()) {
				String field = entry.getKey();
				Object value = entry.getValue();
				int i = header.indexOf(field);
				if (i == -1) {
					continue;
				}
				values[i] = value;
			}
		}
		// fields
		if (hit.has(NODE_FIELDS)) {
			JsonObject fields = hit.getAsJsonObject(NODE_FIELDS);
			for (Entry<String, JsonElement> entry : fields.entrySet()) {
				String field = entry.getKey();
				JsonElement valueElement = entry.getValue();
				Object value = valueElement;
				if (valueElement.isJsonArray()) {
					value = valueElement.getAsJsonArray().get(0);
				}
				int i = header.indexOf(field);
				if (i == -1) {
					continue;
				}
				values[i] = value;
			}
		}
		return values;
	}

	private Map<String, Object> parseHitToMap(JsonObject hit) {
		final Map<String, Object> values = new HashMap<>();
		// _source
		if (hit.has(NODE_SOURCE)) {
			JsonObject source = hit.getAsJsonObject(NODE_SOURCE);
			Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
			Map<String, Object> sourceMap = App.gson().fromJson(source, dataType);
			values.putAll(sourceMap);
		}
		// fields
		if (hit.has(NODE_FIELDS)) {
			JsonObject fields = hit.getAsJsonObject(NODE_FIELDS);
			for (Entry<String, JsonElement> entry : fields.entrySet()) {
				String field = entry.getKey();
				JsonElement valueElement = entry.getValue();
				Object value = valueElement;
				if (valueElement.isJsonArray()) {
					value = valueElement.getAsJsonArray().get(0);
				}
				values.put(field, value);
			}
		}
		return values;
	}

	private Object[] parseBucket(JsonObject bucket) {
		final Object[] values = new Object[header.size()];
		if (bucket.has(NODE_KEY)) {
			JsonObject key = bucket.getAsJsonObject(NODE_KEY);
			for (Entry<String, JsonElement> entry : key.entrySet()) {
				String field = entry.getKey();
				JsonElement value = entry.getValue();
				int i = header.indexOf(field);
				if (i == -1) {
					continue;
				}
				values[i] = value;
			}
		}
		return values;
	}

	private Map<String, Object> parseBucketToMap(JsonObject bucket) {
		final Map<String, Object> values = new HashMap<>();
		JsonObject key = bucket.getAsJsonObject(NODE_KEY);
		Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
		Map<String, Object> keyMap = App.gson().fromJson(key, dataType);
		values.putAll(keyMap);
		return values;
	}

	@Override
	public List<Map<String, Object>> toValues() {
		List<Map<String, Object>> values = new ArrayList<>();
		JsonObject response = App.gson().fromJson(resultString, JsonObject.class);

		// hits
		JsonObject hits = response.getAsJsonObject(NODE_HITS);
		total = hits.get(NODE_TOTAL).getAsLong();
		JsonArray items = hits.getAsJsonArray(NODE_HITS);
		if (items.size() > 0) {
			items.forEach(e -> values.add(parseHitToMap(e.getAsJsonObject())));
		}

		// aggregations
		if (response.has(NODE_AGGREGATIONS)) {
			JsonObject aggregations = response.getAsJsonObject(NODE_AGGREGATIONS);
			JsonObject groupby = aggregations.getAsJsonObject(NODE_GROUPBY);
			JsonArray buckets = groupby.get(NODE_BUCKETS).getAsJsonArray();
			buckets.forEach(e -> values.add(parseBucketToMap(e.getAsJsonObject())));
		}
		return values;
	}

}

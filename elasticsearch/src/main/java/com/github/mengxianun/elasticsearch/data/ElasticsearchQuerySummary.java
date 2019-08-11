package com.github.mengxianun.elasticsearch.data;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class ElasticsearchQuerySummary extends QuerySummary {

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
		Gson gson = new Gson();
		JsonObject response = gson.fromJson(resultString, JsonObject.class);

		// hits
		JsonObject hits = response.getAsJsonObject("hits");
		total = hits.get("total").getAsLong();
		JsonArray items = hits.getAsJsonArray("hits");
		if (items.size() > 0) {
			items.forEach(e -> rows.add(new DefaultRow(header, parseHit(e.getAsJsonObject()))));
		}

		// aggregations
		if (response.has("aggregations")) {
			JsonObject aggregations = response.getAsJsonObject("aggregations");
			JsonObject groupby = aggregations.getAsJsonObject("groupby");
			JsonArray buckets = groupby.get("buckets").getAsJsonArray();
			buckets.forEach(e -> rows.add(new DefaultRow(header, parseBucket(e.getAsJsonObject()))));
		}
		return rows;
	}

	private Object[] parseHit(JsonObject hit) {
		final Object[] values = new Object[header.size()];
		// _source
		JsonObject source = hit.getAsJsonObject("_source");
		for (Entry<String, JsonElement> entry : source.entrySet()) {
			String field = entry.getKey();
			Object value = entry.getValue();
			int i = header.indexOf(field);
			if (i == -1) {
				continue;
			}
			values[i] = value;
		}
		// fields
		JsonObject fields = hit.getAsJsonObject("fields");
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
		return values;
	}

	private Map<String, Object> parseHitToMap(JsonObject hit) {
		final Map<String, Object> values = new HashMap<>();
		// _source
		JsonObject source = hit.getAsJsonObject("_source");
		Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
		Map<String, Object> sourceMap = new Gson().fromJson(source, dataType);
		values.putAll(sourceMap);
		// fields
		JsonObject fields = hit.getAsJsonObject("fields");
		for (Entry<String, JsonElement> entry : fields.entrySet()) {
			String field = entry.getKey();
			JsonElement valueElement = entry.getValue();
			Object value = valueElement;
			if (valueElement.isJsonArray()) {
				value = valueElement.getAsJsonArray().get(0);
			}
			values.put(field, value);
		}
		return values;
	}

	private Object[] parseBucket(JsonObject bucket) {
		final Object[] values = new Object[header.size()];
		JsonObject key = bucket.getAsJsonObject("key");
		for (Entry<String, JsonElement> entry : key.entrySet()) {
			String field = entry.getKey();
			JsonElement value = entry.getValue();
			int i = header.indexOf(field);
			if (i == -1) {
				continue;
			}
			values[i] = value;
		}
		return values;
	}

	private Map<String, Object> parseBucketToMap(JsonObject bucket) {
		final Map<String, Object> values = new HashMap<>();
		JsonObject key = bucket.getAsJsonObject("key");
		Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
		Map<String, Object> keyMap = new Gson().fromJson(key, dataType);
		values.putAll(keyMap);
		return values;
	}

	@Override
	public List<Map<String, Object>> toValues() {
		List<Map<String, Object>> values = new ArrayList<>();
		Gson gson = new Gson();
		JsonObject response = gson.fromJson(resultString, JsonObject.class);

		// hits
		JsonObject hits = response.getAsJsonObject("hits");
		total = hits.get("total").getAsLong();
		JsonArray items = hits.getAsJsonArray("hits");
		if (items.size() > 0) {
			items.forEach(e -> values.add(parseHitToMap(e.getAsJsonObject())));
		}

		// aggregations
		if (response.has("aggregations")) {
			JsonObject aggregations = response.getAsJsonObject("aggregations");
			JsonObject groupby = aggregations.getAsJsonObject("groupby");
			JsonArray buckets = groupby.get("buckets").getAsJsonArray();
			buckets.forEach(e -> values.add(parseBucketToMap(e.getAsJsonObject())));
		}
		return values;
	}

}

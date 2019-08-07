package com.github.mengxianun.elasticsearch.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.github.mengxianun.core.data.AbstractDataSet;
import com.github.mengxianun.core.data.DataSetHeader;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElasticsearchDataSet extends AbstractDataSet {

	private final String resultString;

	public ElasticsearchDataSet(String resultString) {
		this(null, resultString);
	}

	public ElasticsearchDataSet(DataSetHeader header, String resultString) {
		super(header, resultString);
		this.resultString = resultString;
	}

	@Override
	public List<Row> toRows() {
		List<Row> rows = new ArrayList<>();
		Gson gson = new Gson();
		JsonObject response = gson.fromJson(resultString, JsonObject.class);

		// hits
		JsonObject hits = response.getAsJsonObject("hits");
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

}

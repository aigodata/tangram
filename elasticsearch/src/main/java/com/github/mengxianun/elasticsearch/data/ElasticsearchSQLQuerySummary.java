package com.github.mengxianun.elasticsearch.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.data.DefaultHeader;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Header;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.github.mengxianun.core.item.ColumnItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ElasticsearchSQLQuerySummary extends QuerySummary {

	private final String resultString;

	public ElasticsearchSQLQuerySummary(String resultString) {
		this(null, resultString);
	}

	public ElasticsearchSQLQuerySummary(Action action, String resultString) {
		super(action, null);
		this.resultString = resultString;
	}

	private Header createHeader(JsonObject resultObject) {
		JsonArray columnsArray = resultObject.getAsJsonArray("columns");
		List<ColumnItem> columnItems = new ArrayList<>();
		columnsArray.forEach(c -> {
			JsonObject columnObject = c.getAsJsonObject();
			String column = columnObject.get("name").getAsString();
			columnItems.add(new ColumnItem(column));
		});
		return new DefaultHeader(columnItems);
	}

	@Override
	public List<Row> toRows() {
		List<Row> rows = new ArrayList<>();
		JsonObject jsonObject = App.gson().fromJson(resultString, JsonObject.class);
		List<ColumnItem> columnItems = action.getColumnItems();
		Header rowHeader = columnItems.isEmpty() ? createHeader(jsonObject) : header;

		JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
		rowsArray.forEach(e -> rows.add(new DefaultRow(rowHeader, App.gson().fromJson(e, Object[].class))));
		return rows;
	}

	@Override
	public List<Map<String, Object>> toValues() {
		List<Map<String, Object>> values = new ArrayList<>();
		JsonObject jsonObject = App.gson().fromJson(resultString, JsonObject.class);
		JsonArray columnsArray = jsonObject.getAsJsonArray("columns");
		int size = columnsArray.size();
		String[] columns = new String[size];
		for (int i = 0; i < size; i++) {
			JsonObject columnObject = columnsArray.get(i).getAsJsonObject();
			String columnName = columnObject.get("name").getAsString();
			columns[i] = columnName;
		}
		JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
		for (JsonElement rowElement : rowsArray) {
			Map<String, Object> row = new HashMap<>();
			JsonArray rowArray = rowElement.getAsJsonArray();
			for (int i = 0; i < rowArray.size(); i++) {
				row.put(columns[i], rowArray.get(i));
			}
			values.add(row);
		}
		return values;
	}

}

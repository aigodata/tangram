package com.github.mengxianun.elasticsearch.data;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ElasticsearchSQLQuerySummary extends QuerySummary {

	private final String resultString;

	public ElasticsearchSQLQuerySummary(String resultString) {
		this(null, resultString);
	}

	public ElasticsearchSQLQuerySummary(Action action, String resultString) {
		super(action, resultString);
		this.resultString = resultString;
	}

	@Override
	public List<Row> toRows() {
		List<Row> rows = new ArrayList<>();
		JsonObject jsonObject = new Gson().fromJson(resultString, JsonObject.class);
		JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
		Gson gson = new Gson();
		rowsArray.forEach(e -> rows.add(new DefaultRow(null, gson.fromJson(e, Object[].class))));
		return rows;
	}

}

package com.github.mengxianun.elasticsearch.data;

import com.github.mengxianun.core.Action;
import com.google.gson.JsonArray;

public class ElasticsearchSQLLimitQuerySummary extends ElasticsearchSQLQuerySummary {

	private final int start;

	public ElasticsearchSQLLimitQuerySummary(String resultString, int start) {
		this(null, resultString, start);
	}

	public ElasticsearchSQLLimitQuerySummary(Action action, String resultString, int start) {
		super(action, resultString);
		this.start = start;
	}

	public ElasticsearchSQLLimitQuerySummary(Action action, String resultString, int start, long total) {
		super(action, resultString);
		this.start = start;
		this.total = total;
	}

	@Override
	public JsonArray getResultRows() {
		JsonArray limitRows = new JsonArray();
		JsonArray rowsArray = resultObject.getAsJsonArray("rows");
		for (int i = start; i < rowsArray.size(); i++) {
			limitRows.add(rowsArray.get(i));
		}
		return limitRows;
	}

}

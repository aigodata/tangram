package com.github.mengxianun.elasticsearch;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.data.AbstractDataSet;
import com.github.mengxianun.core.data.DataSetHeader;
import com.github.mengxianun.core.data.DefaultRow;
import com.github.mengxianun.core.data.Row;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ElasticsearchSQLDataSet extends AbstractDataSet {

	private final String resultString;

	public ElasticsearchSQLDataSet(String resultString) {
		this(null, resultString);
	}

	public ElasticsearchSQLDataSet(DataSetHeader header, String resultString) {
		super(header, resultString);
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

	@Override
	public List<Object[]> toObjectArrays() {
		List<Object[]> values = new ArrayList<>();
		JsonObject jsonObject = new Gson().fromJson(resultString, JsonObject.class);
		JsonArray rowsArray = jsonObject.getAsJsonArray("rows");
		Gson gson = new Gson();
		rowsArray.forEach(e -> values.add(gson.fromJson(e, Object[].class)));
		return values;
	}

}

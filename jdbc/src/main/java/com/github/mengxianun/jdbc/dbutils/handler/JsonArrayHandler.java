package com.github.mengxianun.jdbc.dbutils.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.mengxianun.jdbc.dbutils.processor.JsonRowProcessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonArrayHandler implements ResultSetHandler<JsonArray> {

	private final JsonRowProcessor convert;

	public JsonArrayHandler() {
		this(new JsonRowProcessor());
	}

	public JsonArrayHandler(JsonRowProcessor convert) {
		this.convert = convert;
	}

	@Override
	public JsonArray handle(ResultSet rs) throws SQLException {
		JsonArray rows = new JsonArray();
		while (rs.next()) {
			rows.add(this.handleRow(rs));
		}
		return rows;
	}

	public JsonObject handleRow(ResultSet rs) throws SQLException {
		return this.convert.toJson(rs);
	}

}

package com.github.mengxianun.jdbc.dbutils.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.github.mengxianun.jdbc.dbutils.processor.JsonRowProcessor;
import com.google.gson.JsonObject;

public class JsonObjectHandler implements ResultSetHandler<JsonObject> {

	private final JsonRowProcessor convert;

	public JsonObjectHandler() {
		this(new JsonRowProcessor());
	}

	public JsonObjectHandler(JsonRowProcessor convert) {
		this.convert = convert;
	}

	@Override
	public JsonObject handle(ResultSet rs) throws SQLException {
		return rs.next() ? this.convert.toJson(rs) : null;
	}

}

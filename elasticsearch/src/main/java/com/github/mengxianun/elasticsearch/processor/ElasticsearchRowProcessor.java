package com.github.mengxianun.elasticsearch.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.github.mengxianun.jdbc.dbutils.processor.JsonRowProcessor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ElasticsearchRowProcessor extends JsonRowProcessor {

	public JsonObject toJson(ResultSet rs) throws SQLException {
		JsonObject jsonObject = new JsonObject();
		ResultSetMetaData rsmd = rs.getMetaData();
		int cols = rsmd.getColumnCount();

		Gson gson = new Gson();
		for (int i = 1; i <= cols; i++) {
			String columnName = rsmd.getColumnLabel(i);
			if (null == columnName || 0 == columnName.length()) {
				columnName = rsmd.getColumnName(i);
			}
			Object value = rs.getObject(i - 1);
			jsonObject.add(columnName.toLowerCase(), gson.toJsonTree(value));
		}

		return jsonObject;
	}

}

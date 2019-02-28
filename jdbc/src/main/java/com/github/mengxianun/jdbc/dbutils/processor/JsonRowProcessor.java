package com.github.mengxianun.jdbc.dbutils.processor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class JsonRowProcessor {

	/**
	 * 将行数据转换为 Json 对象
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
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
			Object value = rs.getObject(i);
			jsonObject.add(columnName, gson.toJsonTree(value));
		}

		return jsonObject;
	}

}

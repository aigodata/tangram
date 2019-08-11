package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc SQL test")
public class SQLTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/sql/";

	@Test
	void testSQL() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject rowObject = result.get(0).getAsJsonObject();
		assertTrue(rowObject.has("ID"));
		assertTrue(rowObject.has("NAME"));
		assertTrue(rowObject.has("AGE"));

	}

}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonObject;

@DisplayName("Jdbc insert test")
public class InsertTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/insert/";

	@Test
	void testInsertTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "insert.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		int primaryKey = result.getAsJsonPrimitive("ID").getAsInt();
		assertEquals(1, primaryKey);
	}

	@Test
	void testInsertKeywordTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "insert_keyword.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		int primaryKey = result.getAsJsonPrimitive("ID").getAsInt();
		assertEquals(1, primaryKey);
	}

}

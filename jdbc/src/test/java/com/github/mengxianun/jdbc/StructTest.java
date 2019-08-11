package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc struct test")
public class StructTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/struct/";

	@Test
	void testStruct() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "struct.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("name"));
		String tableName = result.get("name").getAsString();
		assertEquals("SELECT_TABLE", tableName);
		assertTrue(result.has("columns"));
		JsonArray columns = result.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
		assertTrue(columnObject.has("size"));
		assertTrue(columnObject.has("remarks"));
	}

	@Test
	void testStructs() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "structs.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("catalog"));
		String catalog = result.get("catalog").getAsString();
		assertEquals("TEST", catalog);
		assertTrue(result.has("tables"));
		JsonArray tables = result.getAsJsonArray("tables");
		JsonObject tableObject = tables.get(0).getAsJsonObject();
		assertTrue(tableObject.has("name"));
		assertTrue(tableObject.has("type"));
		assertTrue(tableObject.has("remarks"));

		assertTrue(tableObject.has("columns"));
		JsonArray columns = tableObject.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
		assertTrue(columnObject.has("size"));
		assertTrue(columnObject.has("remarks"));
	}

}

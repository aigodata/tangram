package com.github.mengxianun.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Elasticsearch struct test")
public class StructTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/struct/";

	@Test
	void testStruct() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "struct.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("name"));
		String tableName = result.get("name").getAsString();
		assertEquals("test", tableName);
		assertTrue(result.has("columns"));
		JsonArray columns = result.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
	}

	@Test
	void testStructStar() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "struct_star.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("name"));
		String tableName = result.get("name").getAsString();
		assertEquals("test*", tableName);
		assertTrue(result.has("columns"));
		JsonArray columns = result.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
	}

	@Test
	void testStructs() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "structs.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("tables"));
		JsonArray tables = result.getAsJsonArray("tables");
		JsonObject tableObject = tables.get(0).getAsJsonObject();
		assertTrue(tableObject.has("name"));
		assertTrue(tableObject.has("type"));

		assertTrue(tableObject.has("columns"));
		JsonArray columns = tableObject.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
	}

	@Test
	void testStructsSource() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "structs_source.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("tables"));
		JsonArray tables = result.getAsJsonArray("tables");
		JsonObject tableObject = tables.get(0).getAsJsonObject();
		assertTrue(tableObject.has("name"));
		assertTrue(tableObject.has("type"));

		assertTrue(tableObject.has("columns"));
		JsonArray columns = tableObject.getAsJsonArray("columns");
		JsonObject columnObject = columns.get(0).getAsJsonObject();
		assertTrue(columnObject.has("name"));
		assertTrue(columnObject.has("type"));
	}

}

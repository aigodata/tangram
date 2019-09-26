package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.exception.PermissionException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc permission test")
public class PermissionTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/permission/";

	@Test
	void testSelectAllTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_all_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testUpdateAllTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_all_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testSelectQueryTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_query_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testUpdateQueryTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "update_query_table.json"));
	}

	@Test
	void testSelectAddTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "select_add_table.json"));
	}

	@Test
	void testInsertAddTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "insert_add_table.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		int primaryKey = result.getAsJsonPrimitive("ID").getAsInt();
		assertEquals(1, primaryKey);
	}

	@Test
	void testSelectUpdateTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "select_update_table.json"));
	}

	@Test
	void testUpdateUpdateTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "update_update_table.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toString().toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toString().toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

	@Test
	void testSelectDeleteTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "select_delete_table.json"));
	}

	@Test
	void testDeleteDeleteTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "delete_delete_table.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

	@Test
	void testSelectConditionUserTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_condition_user_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertEquals(1, firstElement.get("ID").getAsLong());
	}

//	@Test
	void testWhereAndOr() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_and_or.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

//	@Test
	void testWhereComplex() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_complex.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

//	@Test
	void testGroup() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_group.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(5, result.size());
	}

//	@Test
	void testOrder() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_order.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 6);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		String maxAge = firstElement.get("AGE").getAsString();
		assertEquals("99", maxAge);
		JsonObject lastElement = result.get(result.size() - 1).getAsJsonObject();
		String minAge = lastElement.get("AGE").getAsString();
		assertEquals("10", minAge);
	}

//	@Test
	void testAlias() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_alias.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "select_alias_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

}

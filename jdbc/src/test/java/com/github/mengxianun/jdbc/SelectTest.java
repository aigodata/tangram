package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc select test")
public class SelectTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/select/";

	@Test
	void testTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testSourceTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_source_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testFields() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_fields.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertTrue(firstElement.has("ID"));
		assertTrue(firstElement.has("myname"));
		assertTrue(firstElement.has("myage"));
	}

	@Test
	void testWhereEqual() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_equal.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
	}

	@Test
	void testWhereNotEqual() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_not_equal.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(5, result.size());
	}

	@Test
	void testWhereLike() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_like.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

	@Test
	void testWhereGreater() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_greater.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(3, result.size());
	}

	@Test
	void testWhereGreaterEqual() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_greater_equal.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(4, result.size());
	}

	@Test
	void testWhereLess() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_less.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
	}

	@Test
	void testWhereLessEqual() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_less_equal.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

	@Test
	void testWhereBetween() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_between.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(3, result.size());
	}

	@Test
	void testWhereIn() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_in.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(3, result.size());
	}

	@Test
	void testWhereNotIn() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_not_in.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(3, result.size());
	}

	@Test
	void testWhereAnd() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_and.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
	}

	@Test
	void testWhereOr() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_or.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

	@Test
	void testWhereAndOr() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_and_or.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

	@Test
	void testWhereComplex() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_where_complex.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
	}

	@Test
	void testGroup() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_group.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(5, result.size());
	}

	@Test
	void testOrder() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_order.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 6);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		String maxAge = firstElement.get("AGE").getAsString();
		assertEquals("99.0", maxAge);
		JsonObject lastElement = result.get(result.size() - 1).getAsJsonObject();
		String minAge = lastElement.get("AGE").getAsString();
		assertEquals("10.0", minAge);
	}
	
	@Test
	void testLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_limit.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(6, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(2, data.size());
		JsonObject firstElement = data.get(0).getAsJsonObject();
		String firstElementName = firstElement.get("NAME").getAsString();
		assertEquals("Brenda", firstElementName);
		JsonObject lastElement = data.get(data.size() - 1).getAsJsonObject();
		String lastElementName = lastElement.get("NAME").getAsString();
		assertEquals("Anne", lastElementName);
	}

	@Test
	void testGroupLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_group_limit.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(5, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(1, data.size());
		JsonObject firstObject = data.get(0).getAsJsonObject();
		assertTrue(firstObject.has("AGE"));
		long age = firstObject.get("AGE").getAsLong();
		assertEquals(99, age);
	}

	@Test
	void testAlias() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_alias.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "select_alias_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

}

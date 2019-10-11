package com.github.mengxianun.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Elasticsearch select test")
public class SelectTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/select/";

	@Test
	void testTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(6, result.size());
	}

	@Test
	void testSourceTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_source_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testStarTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_star_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(12, result.size());
	}

	@Test
	void testMultiTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_multi_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(12, result.size());
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
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertTrue(firstElement.has("age"));
		assertEquals(10, firstElement.get("age").getAsLong());
		assertTrue(firstElement.has("count"));
		assertEquals(1, firstElement.get("count").getAsLong());
	}

	@Test
	void testOrder() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_order.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(6, result.size());
		JsonObject firstElement = result.get(0).getAsJsonObject();
		long maxAge = firstElement.get("age").getAsLong();
		assertEquals(99, maxAge);
		JsonObject lastElement = result.get(result.size() - 1).getAsJsonObject();
		long minAge = lastElement.get("age").getAsLong();
		assertEquals(10, minAge);
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
	}

	@Test
	void testGroupLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_group_limit.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(6, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(1, data.size());
		JsonObject firstObject = data.get(0).getAsJsonObject();
		assertTrue(firstObject.has("age"));
		long age = firstObject.get("age").getAsLong();
		assertEquals(30, age);
	}

	@Test
	void testAlias() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_alias.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject data = result.get(0).getAsJsonObject();
		assertTrue(data.has("aid"));
		assertTrue(data.has("A-name"));
		assertTrue(data.has("age"));
	}

}

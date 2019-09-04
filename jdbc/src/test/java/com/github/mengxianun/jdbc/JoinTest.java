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

@DisplayName("Jdbc join test")
public class JoinTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/join/";

	@Test
	void testJoin() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

	@Test
	void testJoinLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_limit.json");
		JsonObject result = dataResultSet.getJsonData().getAsJsonObject();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(3, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(2, data.size());
	}

	@Test
	void testJoinLimitGroup() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_limit_group.json");
		JsonObject result = dataResultSet.getJsonData().getAsJsonObject();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(3, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(2, data.size());
	}

	@Test
	void testJoinSelf() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_self.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_self_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

	@Test
	void testJoinUserAndRole() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_and_role.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_user_and_role_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

	@Test
	void testJoinUserAndRoleLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_and_role_limit.json");
		JsonObject result = dataResultSet.getJsonData().getAsJsonObject();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(3, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(1, data.size());
	}

	@Test
	void testJoinUserRole() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_role.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_user_role_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

	@Test
	void testJoinUserRoleLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_role_limit.json");
		JsonObject result = dataResultSet.getJsonData().getAsJsonObject();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(3, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(1, data.size());
	}

	@Test
	void testJoinUserAndRoleFields() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_and_role_fields.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_user_and_role_fields_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

	@Test
	void testJoinUserRoleFields() throws JSONException {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join_user_role_fields.json");
		String result = dataResultSet.getJsonData().toString();
		String excepted = readJson(JSON_PARENT_PATH + "join_user_role_fields_result.json");
		JSONCompareResult compareJSON = JSONCompare.compareJSON(result, excepted, JSONCompareMode.LENIENT);
		assertTrue(!compareJSON.failed());
	}

}

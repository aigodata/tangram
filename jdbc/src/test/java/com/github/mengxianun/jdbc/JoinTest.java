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
		String requestJson = readJson(JSON_PARENT_PATH + "join.json");
		String resultJson = dataResultSet.getJsonData().toString();
		JSONCompareResult compareJSON = JSONCompare.compareJSON(requestJson, resultJson, JSONCompareMode.LENIENT);
		System.out.println(compareJSON.getMessage());
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

}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc join test")
public class JoinTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/join/";

	@Test
	void testJoin() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "join.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
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

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonObject;

@DisplayName("Jdbc update test")
public class UpdateTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/update/";

	@Test
	void testUpdateTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "update.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toString().toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toString().toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Global column test")
public class GlobalColumnTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/global/";

	@Test
	void testColumnTimeFormatTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "columns_time.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		String expected = "2019-11-15 20:09:04";
		assertEquals(expected, firstElement.get("MILLISECOND").getAsString());
		assertEquals(expected, firstElement.get("MILLISECOND_STRING").getAsString());
		assertEquals(expected, firstElement.get("TIMESTAMP").getAsString());
	}

}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonObject;

@DisplayName("Jdbc insert test")
public class InsertTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/insert/";

	@Test
	void testInsertTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "insert.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.PRIMARY_KEY.toString().toLowerCase()));
		int primaryKey = result.getAsJsonPrimitive(ResultAttributes.PRIMARY_KEY.toString().toLowerCase()).getAsInt();
		assertEquals(primaryKey, 1);
	}

}

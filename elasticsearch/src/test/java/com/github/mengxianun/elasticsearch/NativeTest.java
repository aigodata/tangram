package com.github.mengxianun.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Elasticsearch native test")
public class NativeTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/native/";

	@Test
	void testNative() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "query_term.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("took"));
		JsonArray hits = result.getAsJsonObject("hits").getAsJsonArray("hits");
		assertEquals(1, hits.size());


	}

}

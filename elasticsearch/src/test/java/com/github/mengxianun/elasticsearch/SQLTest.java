package com.github.mengxianun.elasticsearch;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;

@DisplayName("Elasticsearch SQL test")
public class SQLTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/";

	@Test
	void testTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	@Disabled("for demonstration purposes")
	void skippedTest() {
		// not executed
	}

}

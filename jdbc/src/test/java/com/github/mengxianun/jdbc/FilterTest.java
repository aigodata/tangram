package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;

@DisplayName("Jdbc filter test")
public class FilterTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/filter/";

	@Test
	void testFilter() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "filter_table.json", "filter_table_1.id=1");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 1);
	}

	@Test
	void testFilterRelation() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "filter_table_relation.json", "filter_table_2.id=1");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 1);
	}

	@Test
	void testFilterJoinMainColumn() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "filter_join_main_column.json", "filter_table_1.id=1");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 1);
	}

	@Test
	void testFilterJoinJoinColumn() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "filter_join_join_column.json", "filter_table_2.id=1");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 1);
	}

	@Test
	void testFilterJoinNewColumn() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "filter_join_new_column.json", "filter_table_3.id=1");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(result.size(), 1);
	}

	DataResultSet run(String jsonFile, String filter) {
		String json = readJson(jsonFile);
		DataResultSet dataResultSet = translator.translate(json, filter);
		LOG.info("Json: " + json);
		LOG.info("Result code: " + dataResultSet.getCode());
		LOG.info("Result message: " + dataResultSet.getMessage());
		return dataResultSet;
	}

}

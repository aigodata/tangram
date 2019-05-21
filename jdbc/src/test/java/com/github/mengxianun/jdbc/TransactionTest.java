package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc transaction test")
public class TransactionTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/transaction/";

	@Test
	void testTransaction12() {
		//		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "transaction12.json");
		validTableName("transaction_table_1", "new_name");
		validTableName("transaction_table_2", "new_name");
	}

	@Test
	void testTransaction34() {
		run(JSON_PARENT_PATH + "transaction34.json");
		validTableName("transaction_table_3", "table3_name");
		validTableName("transaction_table_4", "table4_name");
	}

	void validTableName(String table, String name) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("select", table);
		DataResultSet dataResultSet = runJson(jsonObject.toString());
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		JsonObject resultJsonObject = (JsonObject) result.get(0);
		assertEquals(resultJsonObject.get("name").getAsString(), name);
	}

}

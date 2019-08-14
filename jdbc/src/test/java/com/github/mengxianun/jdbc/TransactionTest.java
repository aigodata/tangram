package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.exception.DataException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc transaction test")
public class TransactionTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/transaction/";

	@Test
	void testTransaction12() {
		run(JSON_PARENT_PATH + "transaction12.json");
		validTableName("transaction_table_1", "new_name");
		validTableName("transaction_table_2", "new_name");
	}

	void validTableName(String table, String name) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("select", table);
		DataResultSet dataResultSet = runJson(jsonObject.toString());
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		JsonObject resultJsonObject = (JsonObject) result.get(0);
		assertEquals(name, resultJsonObject.get("NAME").getAsString());
	}

	@Test
	void testTransaction34() {
		assertThrows(DataException.class, () -> run(JSON_PARENT_PATH + "transaction34.json"));
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("detail", "transaction_table_3");
		DataResultSet dataResultSet = runJson(jsonObject.toString());
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertEquals("table3_name", result.get("NAME").getAsString());
	}

	@Test
	void testTransactionPlaceholder() {
		run(JSON_PARENT_PATH + "transaction_placeholder.json");
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("detail", "transaction_placeholder_b");
		DataResultSet dataResultSet = runJson(jsonObject.toString());
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertEquals(1, result.get("A_ID").getAsLong());
	}

}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.exception.PermissionException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc table permission test")
public class PermissionTableTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/permission/table/";

	@Test
	void testSelectAllTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "all_table_select.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testUpdateAllTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "all_table_update.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toString().toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toString().toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

	@Test
	void testSelectQueryTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "query_table_select.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
	}

	@Test
	void testUpdateQueryTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "query_table_update.json"));
	}

	@Test
	void testSelectAddTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "add_table_select.json"));
	}

	@Test
	void testInsertAddTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "add_table_insert.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		int primaryKey = result.getAsJsonPrimitive("ID").getAsInt();
		assertEquals(1, primaryKey);
	}

	@Test
	void testSelectUpdateTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "update_table_select.json"));
	}

	@Test
	void testUpdateUpdateTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "update_table_update.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toString().toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toString().toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

	@Test
	void testSelectDeleteTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "delete_table_select.json"));
	}

	@Test
	void testDeleteDeleteTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "delete_table_delete.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.COUNT.toLowerCase()));
		int count = result.getAsJsonPrimitive(ResultAttributes.COUNT.toLowerCase()).getAsInt();
		assertEquals(1, count);
	}

	@Test
	void testSelectConditionUserTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_condition_user_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertEquals(1, firstElement.get("ID").getAsLong());
	}

	@Test
	void testUpdateConditionUserTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "update_condition_user_table.json"));
	}

	@Test
	void testSelectConditionRoleTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_condition_role_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(3, result.size());
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertEquals(1, firstElement.get("ID").getAsLong());
	}

	@Test
	void testSelectConditionExpressionTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "select_condition_expression_table.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(1, result.size());
	}

	@Test
	void testSelectConditionUserTable2() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "condition_user_table2-select_with_condition.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertEquals(2, result.size());
		assertEquals(2, result.get(0).getAsJsonObject().get("ID").getAsLong());
		assertEquals(3, result.get(1).getAsJsonObject().get("ID").getAsLong());
	}

	@Test
	void testSelectConditionUserTable2WithJoinLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "condition_user_table2-select_with_join_limit.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(2, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(1, data.size());
		assertEquals(3, data.get(0).getAsJsonObject().get("ID").getAsLong());
	}

	@Test
	void testSelectJoinPermissionTable() {
		assertThrows(PermissionException.class,
				() -> run(JSON_PARENT_PATH + "permission_no_table-select_join_permission_table.json"));
	}

}

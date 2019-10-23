package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.exception.PermissionException;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc column permission test")
public class PermissionColumnTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/permission/column/";

	@Test
	void testSelectPermissionColumnTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "permission_column_table-select.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertTrue(firstElement.has("ID"));
		assertTrue(firstElement.has("ALL_COLUMN"));
		assertTrue(firstElement.has("SELECT_COLUMN"));
		assertTrue(!firstElement.has("INSERT_COLUMN"));
		assertTrue(!firstElement.has("UPDATE_COLUMN"));
		assertTrue(!firstElement.has("DELETE_COLUMN"));
	}

	@Test
	void testUpdatePermissionColumnTable() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "permission_column_table-update.json"));
	}

	@Test
	void testInsertPermissionColumnTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "permission_column_table-insert.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		assertEquals(2, result.getAsJsonPrimitive("ID").getAsInt());
	}

	@Test
	void testInsertPermissionColumnTableWithNoPermissionColumn() {
		assertThrows(PermissionException.class, () -> run(JSON_PARENT_PATH + "permission_column_table-insert2.json"));
	}

	@Test
	void testSelectPermissionColumnJoinTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "permission_column_table-select_join.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertTrue(firstElement.has("ID"));
		assertTrue(!firstElement.has("NAME"));
		assertTrue(firstElement.has("PERMISSION_COLUMN_TABLE_ID"));
		assertTrue(firstElement.has("PERMISSION_COLUMN_TABLE_ID__PERMISSION_COLUMN_TABLE"));
		JsonObject permissionColumnTable = firstElement
				.getAsJsonObject("PERMISSION_COLUMN_TABLE_ID__PERMISSION_COLUMN_TABLE");
		assertTrue(permissionColumnTable.has("ALL_COLUMN"));
		assertTrue(permissionColumnTable.has("SELECT_COLUMN"));
		assertTrue(!permissionColumnTable.has("INSERT_COLUMN"));
		assertTrue(!permissionColumnTable.has("UPDATE_COLUMN"));
		assertTrue(!permissionColumnTable.has("DELETE_COLUMN"));
	}

	@Test
	void testSelectPermissionColumnConditionTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "permission_column_condition_table-select.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject firstElement = result.get(0).getAsJsonObject();
		assertTrue(firstElement.has("ID"));
		assertTrue(firstElement.has("COLUMN_USER_1"));
		assertTrue(!firstElement.has("COLUMN_USER_2"));
	}

	@Test
	void testInsertPermissionColumnConditionTable() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "permission_column_condition_table-insert.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has("ID"));
		assertEquals(2, result.getAsJsonPrimitive("ID").getAsInt());
	}

	@Test
	void testInsertPermissionColumnConditionTable2() {
		assertThrows(PermissionException.class,
				() -> run(JSON_PARENT_PATH + "permission_column_condition_table-insert2.json"));
	}

}

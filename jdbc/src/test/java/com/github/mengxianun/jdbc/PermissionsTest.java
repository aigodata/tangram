package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.permission.Permissions;

@DisplayName("Jdbc permissions test")
public class PermissionsTest extends TestSupport {

	@Test
	void testSelectPermissionColumnTable() {
		assertTrue(Permissions.hasTableSelectPermission("permission_all_table"));
		assertTrue(Permissions.hasTableUpdatePermission("permission_all_table"));

		assertTrue(Permissions.hasTableSelectPermission("permission_query_table"));
		assertTrue(!Permissions.hasTableUpdatePermission("permission_query_table"));

		assertTrue(!Permissions.hasTableSelectPermission("permission_add_table"));
		assertTrue(Permissions.hasTableInsertPermission("permission_add_table"));

		assertTrue(!Permissions.hasTableSelectPermission("permission_update_table"));
		assertTrue(Permissions.hasTableUpdatePermission("permission_update_table"));

		assertTrue(!Permissions.hasTableSelectPermission("permission_delete_table"));
		assertTrue(Permissions.hasTableDeletePermission("permission_delete_table"));

		assertTrue(Permissions.hasTableSelectPermission("permission_condition_user_table"));
		assertTrue(!Permissions.hasTableUpdatePermission("permission_condition_user_table"));

		assertTrue(Permissions.hasTableSelectPermission("permission_condition_role_table"));

		assertTrue(Permissions.hasTableSelectPermission("permission_condition_expression_table"));

		assertTrue(Permissions.hasTableSelectPermission("permission_condition_user_table2"));

	}

	@Test
	void testSelectPermissionTable() {
		String conditionUserSQL = Permissions.getTableSelectPermissions("permission_condition_user_table").toSQL();
		assertTrue(conditionUserSQL.startsWith(" AND permission_user.id = "));
		String conditionRoleSQL = Permissions.getTableSelectPermissions("permission_condition_role_table").toSQL();
		assertTrue(conditionRoleSQL.startsWith(" AND permission_role.id in (SELECT"));
		String expressionSQL = Permissions.getTableSelectPermissions("permission_condition_expression_table").toSQL();
		assertEquals(" AND id>1", expressionSQL);
	}

	@Test
	void testColumnPermissions() {
		List<String> permissionColumns = Permissions.getPermissionColumns("permission_column_table");
		List<String> expectedPermissionColumns = Arrays.asList("ID", "ALL_COLUMN", "SELECT_COLUMN", "INSERT_COLUMN",
				"UPDATE_COLUMN", "DELETE_COLUMN");
		assertIterableEquals(expectedPermissionColumns, permissionColumns);

		List<String> selectColumns = Permissions.getSelectColumns("permission_column_table");
		List<String> expectedSelectColumns = Arrays.asList("ID", "ALL_COLUMN", "SELECT_COLUMN");
		assertIterableEquals(expectedSelectColumns, selectColumns);

		List<String> insertColumns = Permissions.getInsertColumns("permission_column_table");
		List<String> expectedInsertColumns = Arrays.asList("ID", "ALL_COLUMN", "INSERT_COLUMN");
		assertIterableEquals(expectedInsertColumns, insertColumns);

		List<String> updateColumns = Permissions.getUpdateColumns("permission_column_table");
		List<String> expectedUpdateColumns = Arrays.asList("ID", "ALL_COLUMN", "UPDATE_COLUMN");
		assertIterableEquals(expectedUpdateColumns, updateColumns);

		List<String> deleteColumns = Permissions.getDeleteColumns("permission_column_table");
		List<String> expectedDeleteColumns = Arrays.asList("ID", "ALL_COLUMN", "DELETE_COLUMN");
		assertIterableEquals(expectedDeleteColumns, deleteColumns);
	}

}

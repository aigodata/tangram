package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
		System.out.println(conditionUserSQL);
		String conditionRoleSQL = Permissions.getTableSelectPermissions("permission_condition_role_table").toSQL();
		System.out.println(conditionRoleSQL);
		String expressionSQL = Permissions.getTableSelectPermissions("permission_condition_expression_table").toSQL();
		System.out.println(expressionSQL);
	}

}

package com.github.mengxianun.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.permission.Permissions;

@DisplayName("Jdbc permissions test")
public class PermissionsTest extends TestSupport {

	@Test
	void testSelectPermissionColumnTable() {
		Permissions.hasTableSelectPermission("permission_all_table");
	}

}

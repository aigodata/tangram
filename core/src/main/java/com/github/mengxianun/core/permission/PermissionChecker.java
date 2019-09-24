package com.github.mengxianun.core.permission;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.schema.Table;
import com.google.common.collect.HashBasedTable;

public final class PermissionChecker {

	private PermissionChecker() {
		throw new AssertionError();
	}

	public static boolean check(Action action, HashBasedTable<String, String, List<TablePermission>> tablePermissions) {
		PermissionPolicy policy = App.getPermissionPolicy();
		if (policy == null || policy == PermissionPolicy.ALLOW_ALL) {
			return true;
		}
		if (policy == PermissionPolicy.DENY_ALL) {
			return false;
		}
		Table primaryTable = action.getPrimaryTable();
		List<Table> joinTables = action.getJoinTables();
		List<Table> actionTables = new ArrayList<>();
		actionTables.add(primaryTable);
		actionTables.addAll(joinTables);

		com.github.mengxianun.core.permission.Action executeAction = getExecuteAction(action);

		boolean check = check(executeAction, actionTables, tablePermissions);
		if (check) {

		}
		return check;
	}

	private static boolean check(com.github.mengxianun.core.permission.Action executeAction, List<Table> actionTables, HashBasedTable<String, String, List<TablePermission>> tablePermissions) {
		for (Table table : actionTables) {
			String source = getSource(table);
			List<TablePermission> permissions = tablePermissions.get(source, table.getName());
			for (TablePermission tablePermission : permissions) {
				com.github.mengxianun.core.permission.Action tableAction = tablePermission.getAction();
				if (tableAction != com.github.mengxianun.core.permission.Action.ALL || tableAction != executeAction) {
					return false;
				}
			}
		}
		return true;
	}

	private static void addConditions(com.github.mengxianun.core.permission.Action executeAction, List<Table> actionTables, HashBasedTable<String, String, List<TablePermission>> tablePermissions) {
		for (Table table : actionTables) {
			String source = getSource(table);
			List<TablePermission> permissions = tablePermissions.get(source, table.getName());
			for (TablePermission tablePermission : permissions) {
				List<Condition> conditions = tablePermission.getConditions();
				for (Condition condition : conditions) {
					condition.getClass();
				}
				//				if (tableAction != com.github.mengxianun.core.permission.Action.ALL || tableAction != executeAction) {
				//					return false;
				//				}
			}
		}
	}

	private static String getSource(Table table) {
		Map<String, DataContext> dataContexts = App.getDataContexts();
		for (Entry<String, DataContext> entry : dataContexts.entrySet()) {
			String source = entry.getKey();
			DataContext dataContext = entry.getValue();
			if (dataContext.hasTable(table)) {
				return source;
			}
		}
		return null;
	}

	private static com.github.mengxianun.core.permission.Action getExecuteAction(Action action) {
		if (action.isQuery()) {
			return com.github.mengxianun.core.permission.Action.QUERY;
		} else if (action.isInsert()) {
			return com.github.mengxianun.core.permission.Action.ADD;
		} else if (action.isUpdate()) {
			return com.github.mengxianun.core.permission.Action.UPDATE;
		} else if (action.isDelete()) {
			return com.github.mengxianun.core.permission.Action.DELETE;
		}
		return com.github.mengxianun.core.permission.Action.ALL;
	}

}

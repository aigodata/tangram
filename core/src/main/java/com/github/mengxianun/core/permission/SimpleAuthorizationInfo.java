package com.github.mengxianun.core.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SimpleAuthorizationInfo implements AuthorizationInfo {

	// The name of the data source where the user table resides
	private final String userSource;
	// User table name
	private final String userTable;
	// User table id field name
	private final String userIdColumn;
	// Method to get the current user table id
	private final Supplier<Object> userIdSupplier;
	// The method that gets the permissions of the current user data table
	private final Supplier<List<TablePermission>> tablePermissionsSupplier;
	// The method to get permissions for the current user data table column
	private final Supplier<List<ColumnPermission>> columnPermissionsSupplier;

	// Key -> userId
	private final Map<Object, Map<TableKey, List<TablePermission>>> userTablePermissions = new ConcurrentHashMap<>();
	private final Map<Object, Map<TableKey, List<ColumnPermission>>> userColumnPermissions = new ConcurrentHashMap<>();

	public SimpleAuthorizationInfo(String userTable, String userIdColumn, Supplier<Object> userIdSupplier,
			Supplier<List<TablePermission>> tablePermissionsSupplier) {
		this(null, userTable, userIdColumn, userIdSupplier, tablePermissionsSupplier, () -> Collections.emptyList());
	}

	public SimpleAuthorizationInfo(String userSource, String userTable, String userIdColumn,
			Supplier<Object> userIdSupplier, Supplier<List<TablePermission>> tablePermissionsSupplier,
			Supplier<List<ColumnPermission>> columnPermissionsSupplier) {
		this.userSource = userSource;
		this.userTable = userTable;
		this.userIdColumn = userIdColumn;
		this.userIdSupplier = userIdSupplier;
		this.tablePermissionsSupplier = tablePermissionsSupplier;
		this.columnPermissionsSupplier = columnPermissionsSupplier;
		initPermissions();
	}

	private void initPermissions() {
		Object userId = getUserId();
		// Table
		Map<TableKey, List<TablePermission>> tableKeyPermissions = new HashMap<>();
		for (TablePermission tablePermission : tablePermissionsSupplier.get()) {
			String source = tablePermission.source();
			String table = tablePermission.table();
			TableKey tableKey = TableKey.create(source, table);
			if (tableKeyPermissions.containsKey(tableKey)) {
				tableKeyPermissions.get(tableKey).add(tablePermission);
			} else {
				List<TablePermission> tablePermissions = new ArrayList<>();
				tablePermissions.add(tablePermission);
				tableKeyPermissions.put(tableKey, tablePermissions);
			}
		}
		userTablePermissions.put(userId, tableKeyPermissions);

		// Column
		Map<TableKey, List<ColumnPermission>> columnKeyPermissions = new HashMap<>();
		for (ColumnPermission columnPermission : columnPermissionsSupplier.get()) {
			String source = columnPermission.source();
			String table = columnPermission.table();
			TableKey tableKey = TableKey.create(source, table);
			if (columnKeyPermissions.containsKey(tableKey)) {
				columnKeyPermissions.get(tableKey).add(columnPermission);
			} else {
				List<ColumnPermission> columnPermissions = new ArrayList<>();
				columnPermissions.add(columnPermission);
				columnKeyPermissions.put(tableKey, columnPermissions);
			}
		}
		userColumnPermissions.put(userId, columnKeyPermissions);
	}

	@Override
	public String getUserSource() {
		return userSource;
	}

	@Override
	public String getUserTable() {
		return userTable;
	}

	@Override
	public String getUserIdColumn() {
		return userIdColumn;
	}

	@Override
	public Object getUserId() {
		return userIdSupplier.get();
	}

	@Override
	public List<TablePermission> getCurrentTablePermissions() {
		return userTablePermissions.get(getUserId()).values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<TablePermission> getCurrentTablePermissions(String source, String table) {
		TableKey tableKey = TableKey.create(source, table);
		Map<TableKey, List<TablePermission>> tableKeyPermissions = userTablePermissions.get(getUserId());
		if (tableKeyPermissions.containsKey(tableKey)) {
			return tableKeyPermissions.get(tableKey);
		}
		return Collections.emptyList();
	}

	@Override
	public List<ColumnPermission> getCurrentColumnPermissions() {
		return userColumnPermissions.get(getUserId()).values().stream().flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public List<ColumnPermission> getCurrentColumnPermissions(String source, String table) {
		TableKey tableKey = TableKey.create(source, table);
		Map<TableKey, List<ColumnPermission>> columnKeyPermissions = userColumnPermissions.get(getUserId());
		if (columnKeyPermissions.containsKey(tableKey)) {
			return columnKeyPermissions.get(tableKey);
		}
		return Collections.emptyList();
	}

	@Override
	public void refresh() {
		userTablePermissions.clear();
		userColumnPermissions.clear();
		initPermissions();
	}

}

package com.github.mengxianun.core.permission;

import java.util.List;
import java.util.function.Supplier;

public class SimpleAuthorizationInfo implements AuthorizationInfo {

	private final String userSource;
	private final String userTable;
	private final String userIdColumn;
	private final Supplier<Object> userIdSupplier;
	private final Supplier<List<TablePermission>> tablePermissionsSupplier;

	public SimpleAuthorizationInfo(String userTable, String userIdColumn, Supplier<Object> userIdSupplier,
			Supplier<List<TablePermission>> tablePermissionsSupplier) {
		this(null, userTable, userIdColumn, userIdSupplier, tablePermissionsSupplier);
	}

	public SimpleAuthorizationInfo(String userSource, String userTable, String userIdColumn,
			Supplier<Object> userIdSupplier, Supplier<List<TablePermission>> tablePermissionsSupplier) {
		this.userSource = userSource;
		this.userTable = userTable;
		this.userIdColumn = userIdColumn;
		this.userIdSupplier = userIdSupplier;
		this.tablePermissionsSupplier = tablePermissionsSupplier;
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
	public Supplier<Object> getUserIdSupplier() {
		return userIdSupplier;
	}

	@Override
	public Supplier<List<TablePermission>> getTablePermissionsSupplier() {
		return tablePermissionsSupplier;
	}

	public Object getUserId() {
		return getUserIdSupplier().get();
	}

	public List<TablePermission> getTablePermissions() {
		return getTablePermissionsSupplier().get();
	}

}

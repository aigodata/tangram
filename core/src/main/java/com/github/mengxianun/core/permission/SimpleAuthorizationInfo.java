package com.github.mengxianun.core.permission;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class SimpleAuthorizationInfo implements AuthorizationInfo {

	private static final String TABLE_PERMISSIONS_CACHE_KEY = "table_permissions_cache";
	private static final String COLUMN_PERMISSIONS_CACHE_KEY = "column_permissions_cache";
	private final String userSource;
	private final String userTable;
	private final String userIdColumn;
	private final Supplier<Object> userIdSupplier;
	private final Supplier<List<TablePermission>> tablePermissionsSupplier;
	private final Supplier<List<ColumnPermission>> columnPermissionsSupplier;
	private final LoadingCache<String, List<TablePermission>> tablePermissionsCache = CacheBuilder.newBuilder()
			.maximumSize(10000).expireAfterAccess(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<TablePermission>>() {

				public List<TablePermission> load(String key) throws Exception {
					return loadTablePermissions();
				}
			});
	private final LoadingCache<String, List<ColumnPermission>> columnPermissionsCache = CacheBuilder.newBuilder()
			.maximumSize(10000).expireAfterAccess(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<ColumnPermission>>() {

				public List<ColumnPermission> load(String key) throws Exception {
					return loadColumnPermissions();
				}
			});

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
	public List<TablePermission> getTablePermissions() {
		try {
			return tablePermissionsCache.get(TABLE_PERMISSIONS_CACHE_KEY);
		} catch (ExecutionException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void refreshTablePermissions() {
		tablePermissionsCache.invalidate(TABLE_PERMISSIONS_CACHE_KEY);
	}

	public List<TablePermission> loadTablePermissions() {
		return tablePermissionsSupplier.get();
	}

	@Override
	public List<ColumnPermission> getColumnPermissions() {
		try {
			return columnPermissionsCache.get(COLUMN_PERMISSIONS_CACHE_KEY);
		} catch (ExecutionException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void refreshColumnPermissions() {
		columnPermissionsCache.invalidate(COLUMN_PERMISSIONS_CACHE_KEY);
	}

	public List<ColumnPermission> loadColumnPermissions() {
		return columnPermissionsSupplier.get();
	}

}

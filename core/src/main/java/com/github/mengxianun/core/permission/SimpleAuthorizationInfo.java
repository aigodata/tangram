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
	private final String userSource;
	private final String userTable;
	private final String userIdColumn;
	private final Supplier<Object> userIdSupplier;
	private final Supplier<List<TablePermission>> tablePermissionsSupplier;
	private final LoadingCache<String, List<TablePermission>> tablePermissionsCache = CacheBuilder.newBuilder()
			.maximumSize(10000).expireAfterAccess(1, TimeUnit.DAYS)
			.build(new CacheLoader<String, List<TablePermission>>() {

				public List<TablePermission> load(String key) throws Exception {
					return loadTablePermissions();
				}
			});

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

}

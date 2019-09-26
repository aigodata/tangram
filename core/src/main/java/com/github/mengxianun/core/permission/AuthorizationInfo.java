package com.github.mengxianun.core.permission;

import java.util.List;
import java.util.function.Supplier;

public interface AuthorizationInfo {

	public String getUserSource();

	public String getUserTable();

	public String getUserIdColumn();

	public Supplier<Object> getUserIdSupplier();

	public Supplier<List<TablePermission>> getTablePermissionsSupplier();

}

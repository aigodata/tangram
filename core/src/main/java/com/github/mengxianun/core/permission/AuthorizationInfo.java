package com.github.mengxianun.core.permission;

import java.util.List;

public interface AuthorizationInfo {

	public String getUserSource();

	public String getUserTable();

	public String getUserIdColumn();

	public Object getUserId();

	public List<TablePermission> getCurrentTablePermissions();

	public List<TablePermission> getCurrentTablePermissions(String source, String table);

	public List<ColumnPermission> getCurrentColumnPermissions();

	public List<ColumnPermission> getCurrentColumnPermissions(String source, String table);

	public void refresh();

}

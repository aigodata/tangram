package com.github.mengxianun.core.permission;

import java.util.List;

public interface AuthorizationInfo {

	public String getUserSource();

	public String getUserTable();

	public String getUserIdColumn();

	public Object getUserId();

	public List<TablePermission> getTablePermissions();

	public void refreshTablePermissions();

	public List<ColumnPermission> getColumnPermissions();

	public void refreshColumnPermissions();

}

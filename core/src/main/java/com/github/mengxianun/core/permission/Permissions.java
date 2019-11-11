package com.github.mengxianun.core.permission;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.PermissionException;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.RequestKeyword;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

/**
 * Permission tool class, based on the current user
 * 
 * @author mengxiangyun
 *
 */
public class Permissions {

	private Permissions() {
		throw new AssertionError();
	}

	/**
	 * Whether the current user has table permissions
	 * 
	 * @param table
	 * @return
	 */
	public static boolean hasTablePermission(String table) {
		return hasTablePermission(null, table);
	}

	/**
	 * Whether the current user has table permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static boolean hasTablePermission(String source, String table) {
		return App.hasTablePermissions(source, table);
	}

	/**
	 * Whether the current user has table select permissions
	 * 
	 * @param table
	 * @return
	 */
	public static boolean hasTableSelectPermission(String table) {
		return hasTableSelectPermission(null, table);
	}

	/**
	 * Whether the current user has table select permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static boolean hasTableSelectPermission(String source, String table) {
		return hasTableActionPermission(source, table, Action.SELECT);
	}

	/**
	 * Whether the current user has table insert permissions
	 * 
	 * @param table
	 * @return
	 */
	public static boolean hasTableInsertPermission(String table) {
		return hasTableInsertPermission(null, table);
	}

	/**
	 * Whether the current user has table insert permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static boolean hasTableInsertPermission(String source, String table) {
		return hasTableActionPermission(source, table, Action.INSERT);
	}

	/**
	 * Whether the current user has table update permissions
	 * 
	 * @param table
	 * @return
	 */
	public static boolean hasTableUpdatePermission(String table) {
		return hasTableUpdatePermission(null, table);
	}
	
	/**
	 * Whether the current user has table update permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static boolean hasTableUpdatePermission(String source, String table) {
		return hasTableActionPermission(source, table, Action.UPDATE);
	}

	/**
	 * Whether the current user has table delete permissions
	 * 
	 * @param table
	 * @return
	 */
	public static boolean hasTableDeletePermission(String table) {
		return hasTableDeletePermission(null, table);
	}

	/**
	 * Whether the current user has table delete permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static boolean hasTableDeletePermission(String source, String table) {
		return hasTableActionPermission(source, table, Action.DELETE);
	}

	/**
	 * Whether the current user has table action permissions
	 * 
	 * @param source
	 *            data source
	 * @param table
	 *            source table
	 * @param action
	 *            operation action
	 * @return
	 */
	private static boolean hasTableActionPermission(String source, String table, Action action) {
		if (hasTablePermission(source, table)) {
			List<TablePermission> tablePermissions = App.getTablePermissions(source, table);
			return tablePermissions.parallelStream()
					.anyMatch(e -> e.action() == Action.ALL || e.action() == action);
		}
		return false;
	}

	/**
	 * Get the current user's table permissions
	 * 
	 * @param table
	 * @return
	 */
	public static TablePermissions getTablePermissions(String table) {
		return getTablePermissions(null, table);
	}

	/**
	 * Get the current user's table permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static TablePermissions getTablePermissions(String source, String table) {
		List<TablePermission> tablePermissions = App.getTablePermissions(source, table);
		List<ConnectorCondition> conditions = tablePermissions.stream().flatMap(e -> e.conditions().stream())
				.collect(Collectors.toList());
		return new TablePermissions(source, table, conditions);
	}

	/**
	 * Get the current user's table select permissions
	 * 
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableSelectPermissions(String table) {
		return getTableSelectPermissions(null, table);
	}

	/**
	 * Get the current user's table select permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableSelectPermissions(String source, String table) {
		return getTableActionPermissions(source, table, Action.SELECT);
	}

	/**
	 * Get the current user's table insert permissions
	 * 
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableInsertPermissions(String table) {
		return getTableInsertPermissions(null, table);
	}

	/**
	 * Get the current user's table insert permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableInsertPermissions(String source, String table) {
		return getTableActionPermissions(source, table, Action.INSERT);
	}

	/**
	 * Get the current user's table update permissions
	 * 
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableUpdatePermissions(String table) {
		return getTableUpdatePermissions(null, table);
	}

	/**
	 * Get the current user's table update permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableUpdatePermissions(String source, String table) {
		return getTableActionPermissions(source, table, Action.UPDATE);
	}

	/**
	 * Get the current user's table delete permissions
	 * 
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableDeletePermissions(String table) {
		return getTableDeletePermissions(null, table);
	}

	/**
	 * Get the current user's table delete permissions
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static TablePermissions getTableDeletePermissions(String source, String table) {
		return getTableActionPermissions(source, table, Action.DELETE);
	}

	/**
	 * Get the current user's table action permissions
	 * 
	 * @param source
	 * @param table
	 * @param action
	 * @return
	 */
	private static TablePermissions getTableActionPermissions(String source, String table, Action action) {
		List<TablePermission> tablePermissions = App.getTablePermissions(source, table);
		List<ConnectorCondition> conditions = tablePermissions.stream()
				.filter(e -> e.action() == Action.ALL || e.action() == action)
				.flatMap(e -> e.conditions().stream())
				.collect(Collectors.toList());
		return new TablePermissions(source, table, conditions);
	}

	/**
	 * Gets the SQL of the permission query for table
	 * 
	 * @param table
	 * @return
	 */
	public static String getTableSelectSQL(String table) {
		return getTableSelectSQL(null, table);
	}

	/**
	 * Gets the SQL of the permission query for table
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static String getTableSelectSQL(String source, String table) {
		if (Strings.isNullOrEmpty(source)) {
			source = App.getDefaultDataSource();
		}
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name().toLowerCase(), source + "." + table);
		DataContext dataContext = App.getDataContext(source);
		SimpleInfo simpleInfo = SimpleParser.parse(jsonObject);
		PermissionCheckResult checkResult = PermissionChecker.checkWithResult(simpleInfo);
		if (checkResult.pass()) {
			simpleInfo = checkResult.simpleInfo();
		} else {
			String message = String.format("Table [%s.%s] has no [%s] permission", source, table,
					simpleInfo.operation());
			throw new PermissionException(message);
		}
		com.github.mengxianun.core.Action action = (com.github.mengxianun.core.Action) new CRUDActionParser(simpleInfo,
				dataContext).parse();
		action.build();
		try {
			return SQLParser.fill(action.getSql(), action.getParams().toArray());
		} catch (SQLException e) {
			throw new DataException("SQL build fail", e);
		}
	}

	/**
	 * Get the current user's table permissions where
	 * 
	 * @param table
	 * @return
	 */
	public static String getWhere(String table) {
		return getWhere(null, table);
	}

	/**
	 * Get the current user's table permissions where
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static String getWhere(String source, String table) {
		return getTablePermissions(source, table).toSQL();
	}

	/**
	 * Get the current user's table permissions condition sql
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static String getTableConditionSessionSql(String source, String table, String column) {
		AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
		String userTable = authorizationInfo.getUserTable();
		String userIdColumn = authorizationInfo.getUserIdColumn();
		Object userId = authorizationInfo.getUserId();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name().toLowerCase(), table);
		jsonObject.addProperty(RequestKeyword.FIELDS.lowerName(), column);
		jsonObject.addProperty(RequestKeyword.JOIN.lowerName(), userTable);
		jsonObject.addProperty(RequestKeyword.WHERE.lowerName(), userTable + "." + userIdColumn + "=" + userId);
		DataContext dataContext = Strings.isNullOrEmpty(source) ? App.getDefaultDataContext()
				: App.getDataContext(source);
		SimpleInfo simpleInfo = SimpleParser.parse(jsonObject);
		com.github.mengxianun.core.Action action = (com.github.mengxianun.core.Action) new CRUDActionParser(simpleInfo,
				dataContext).parse();
		action.build();
		try {
			return SQLParser.fill(action.getSql(), action.getParams().toArray());
		} catch (SQLException e) {
			throw new DataException("Condition sql build fail");
		}
	}

	/**
	 * Condition to SQL
	 * 
	 * @param condition
	 * @return
	 */
	private static String toConditionSQL(Condition condition) {
		StringBuilder builder = new StringBuilder();
		if (condition instanceof TableCondition) {
			TableCondition tableCondition = (TableCondition) condition;
			String source = tableCondition.source();
			String table = tableCondition.table();
			String column = tableCondition.column();
			Object value = tableCondition.value();
			if (value != null && "$session".equalsIgnoreCase(value.toString())) { // session condition
				AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
				String userTable = authorizationInfo.getUserTable();
				Object userId = authorizationInfo.getUserId();
				if (userTable.equalsIgnoreCase(table)) {
					value = userId;
					builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" = ")
							.append(value);
				} else { // get statement value
					value = getTableConditionSessionSql(source, table, column);
					builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" in ")
							.append('(').append(value).append(')');
				}
			} else { // Specific conditions
				builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" = ")
						.append(value);
			}
		} else if (condition instanceof ExpressionCondition) {
			builder.append(((ExpressionCondition) condition).expression());
		}
		return builder.toString();
	}

	public static class TablePermissions {

		private String source;
		private String table;
		private List<ConnectorCondition> tableConditions;

		public TablePermissions(String source, String table, List<ConnectorCondition> tableConditions) {
			this.source = source;
			this.table = table;
			this.tableConditions = tableConditions;
		}

		public String getSource() {
			return source;
		}

		public String getTable() {
			return table;
		}

		public List<ConnectorCondition> getTableConditions() {
			return tableConditions;
		}

		public String toSQL() {
			if (tableConditions.isEmpty()) {
				return "";
			}
			StringBuilder builder = new StringBuilder();
			for (ConnectorCondition connectorCondition : tableConditions) {
				Connector connector = connectorCondition.connector();
				Condition condition = connectorCondition.condition();

				builder.append(" ").append(connector).append(" ");
				builder.append(toConditionSQL(condition));
			}
			return builder.toString();
		}

	}

	/**
	 * Whether the current user has column permissions
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnPermission(String table, String column) {
		return hasColumnPermission(null, table, column);
	}

	/**
	 * Whether the current user has column permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnPermission(String source, String table, String column) {
		return App.hasColumnPermissions(source, table, column);
	}

	/**
	 * Whether the current user has column select permissions
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnSelectPermission(String table, String column) {
		return hasColumnSelectPermission(null, table, column);
	}

	/**
	 * Whether the current user has column select permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnSelectPermission(String source, String table, String column) {
		return hasColumnActionPermission(source, table, column, Action.SELECT);
	}

	/**
	 * Whether the current user has column insert permissions
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnInsertPermission(String table, String column) {
		return hasColumnInsertPermission(null, table, column);
	}

	/**
	 * Whether the current user has column insert permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnInsertPermission(String source, String table, String column) {
		return hasColumnActionPermission(source, table, column, Action.INSERT);
	}

	/**
	 * Whether the current user has column update permissions
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnUpdatePermission(String table, String column) {
		return hasColumnUpdatePermission(null, table, column);
	}

	/**
	 * Whether the current user has column update permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnUpdatePermission(String source, String table, String column) {
		return hasColumnActionPermission(source, table, column, Action.UPDATE);
	}

	/**
	 * Whether the current user has column delete permissions
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnDeletePermission(String table, String column) {
		return hasColumnDeletePermission(null, table, column);
	}

	/**
	 * Whether the current user has column delete permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @return
	 */
	public static boolean hasColumnDeletePermission(String source, String table, String column) {
		return hasColumnActionPermission(source, table, column, Action.DELETE);
	}

	/**
	 * Whether the current user has column action permissions
	 * 
	 * @param source
	 * @param table
	 * @param column
	 * @param action
	 * @return
	 */
	private static boolean hasColumnActionPermission(String source, String table, String column, Action action) {
		if (hasColumnPermission(source, table, column)) {
			List<ColumnPermission> columnPermissions = App.getColumnPermissions(source, table, column);
			return columnPermissions.parallelStream().anyMatch(e -> e.action() == Action.ALL || e.action() == action);
		}
		return false;
	}

	/**
	 * Get the current user's permissions columns
	 * 
	 * @param table
	 * @return
	 */
	public static List<String> getPermissionColumns(String table) {
		return getPermissionColumns(null, table);
	}

	/**
	 * Get the current user's permissions columns
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static List<String> getPermissionColumns(String source, String table) {
		boolean configured = App.hasTableColumnPermissions(source, table);
		if (!configured) {
			String dataContextName = Strings.isNullOrEmpty(source) ? App.getDefaultDataSource() : source;
			Table sourceTable = App.getTable(dataContextName, table);
			if (sourceTable == null) {
				return Collections.emptyList();
			}
			return sourceTable.getColumnNames();
		}
		List<ColumnPermission> columnPermissions = App.getColumnPermissions(source, table);
		return columnPermissions.stream().map(ColumnPermission::column).collect(Collectors.toList());
	}

	/**
	 * Get the current user's select permissions columns
	 * 
	 * @param table
	 * @return
	 */
	public static List<String> getSelectColumns(String table) {
		return getSelectColumns(null, table);
	}

	/**
	 * Get the current user's select permissions columns
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static List<String> getSelectColumns(String source, String table) {
		return getActionColumns(source, table, Action.SELECT);
	}

	/**
	 * Get the current user's insert permissions columns
	 * 
	 * @param table
	 * @return
	 */
	public static List<String> getInsertColumns(String table) {
		return getInsertColumns(null, table);
	}

	/**
	 * Get the current user's insert permissions columns
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static List<String> getInsertColumns(String source, String table) {
		return getActionColumns(source, table, Action.INSERT);
	}

	/**
	 * Get the current user's update permissions columns
	 * 
	 * @param table
	 * @return
	 */
	public static List<String> getUpdateColumns(String table) {
		return getUpdateColumns(null, table);
	}

	/**
	 * Get the current user's update permissions columns
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static List<String> getUpdateColumns(String source, String table) {
		return getActionColumns(source, table, Action.UPDATE);
	}

	/**
	 * Get the current user's delete permissions columns
	 * 
	 * @param table
	 * @return
	 */
	public static List<String> getDeleteColumns(String table) {
		return getDeleteColumns(null, table);
	}

	/**
	 * Get the current user's delete permissions columns
	 * 
	 * @param source
	 * @param table
	 * @return
	 */
	public static List<String> getDeleteColumns(String source, String table) {
		return getActionColumns(source, table, Action.DELETE);
	}

	/**
	 * Get the current user's action permissions columns
	 * 
	 * @param source
	 * @param table
	 * @param action
	 * @return
	 */
	private static List<String> getActionColumns(String source, String table, Action action) {
		Table sourceTable = App.getTable(source, table);
		if (sourceTable == null) {
			return Collections.emptyList();
		}
		return sourceTable.getColumnNames().stream()
				.filter(e -> PermissionChecker.checkColumn(source, table, e, action)).collect(Collectors.toList());
	}

}

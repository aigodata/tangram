package com.github.mengxianun.core.permission;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.RequestKeyword;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

/**
 * 权限工具类, 基于当前用户
 * 
 * @author mengxiangyun
 *
 */
public class Permissions {

	private Permissions() {
		throw new AssertionError();
	}

	public static boolean hasTablePermission(String table) {
		return hasTablePermission(null, table);
	}

	public static boolean hasTablePermission(String source, String table) {
		return App.hasTablePermissions(source, table);
	}

	public static boolean hasTableSelectPermission(String table) {
		return hasActionPermission(table, Action.SELECT);
	}

	public static boolean hasTableInsertPermission(String table) {
		return hasActionPermission(table, Action.INSERT);
	}

	public static boolean hasTableUpdatePermission(String table) {
		return hasActionPermission(table, Action.UPDATE);
	}

	public static boolean hasTableDeletePermission(String table) {
		return hasActionPermission(table, Action.DELETE);
	}

	private static boolean hasActionPermission(String table, Action action) {
		if (hasTablePermission(table)) {
			List<TablePermission> tablePermissions = App.getTablePermissions(null, table);
			return tablePermissions.parallelStream()
					.anyMatch(e -> e.action() == Action.ALL || e.action() == action);
		}
		return false;
	}

	public static TablePermissions getTablePermissions(String table) {
		List<TablePermission> tablePermissions = App.getTablePermissions(null, table);
		List<ConnectorCondition> conditions = tablePermissions.stream().flatMap(e -> e.conditions().stream())
				.collect(Collectors.toList());
		return new TablePermissions(null, table, conditions);
	}

	public static TablePermissions getTableSelectPermissions(String table) {
		return getActionPermissions(table, Action.SELECT);
	}

	public static TablePermissions getTableInsertPermissions(String table) {
		return getActionPermissions(table, Action.INSERT);
	}

	public static TablePermissions getTableUpdatePermissions(String table) {
		return getActionPermissions(table, Action.UPDATE);
	}

	public static TablePermissions getTableDeletePermissions(String table) {
		return getActionPermissions(table, Action.DELETE);
	}

	public static TablePermissions getActionPermissions(String table, Action action) {
		List<TablePermission> tablePermissions = App.getTablePermissions(null, table);
		List<ConnectorCondition> conditions = tablePermissions.stream()
				.filter(e -> e.action() == Action.ALL || e.action() == action)
				.flatMap(e -> e.conditions().stream())
				.collect(Collectors.toList());
		return new TablePermissions(null, table, conditions);
	}

	public static String getWhere(String table) {
		// to do
		return null;
	}

	/**
	 * 获取可以查询的列
	 * 
	 * @param table
	 * @return
	 */
	public static Set<String> getQueryColumns(String table) {
		// to do
		return null;
	}

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
					//					FilterInfo filterInfo = FilterInfo.create(ConditionInfo
					//							.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value));
					builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" = ")
							.append(value);
				} else { // get statement value
					value = getTableConditionSessionSql(source, table, column);
					//					StatementValueConditionInfo statementValueConditionInfo = StatementValueConditionInfo
					//							.create(ColumnInfo.create(source, table, column, null), Operator.IN, conditionSql);
					builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" in ")
							.append('(').append(value).append(')');
				}
			} else { // Specific conditions
				//				FilterInfo filterInfo = FilterInfo.create(ConditionInfo
				//						.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value));
				builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" = ")
						.append(value);
			}
		} else if (condition instanceof ExpressionCondition) {
			//			ExpressionCondition expressionCondition = (ExpressionCondition) condition;
			//			String expression = expressionCondition.expression();
			//			ConditionInfo conditionInfo = new SimpleParser("").parseCondition(expression);
			//			FilterInfo filterInfo = FilterInfo.create(conditionInfo);
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
				//				if (condition instanceof TableCondition) {
				//					TableCondition tableCondition = (TableCondition) condition;
				//					Object value = tableCondition.value();
				//					builder.append(tableCondition.table()).append('.').append(tableCondition.column()).append(" = ")
				//							.append(value);
				//					////////////////////////////////////////////////////
				//					////////////////////////////////////////////////////
				//					////////////////////////////////////////////////////
				//				} else if (condition instanceof ExpressionCondition) {
				//					builder.append(((ExpressionCondition) condition).expression());
				//				}
			}
			return builder.toString();
		}

	}

}

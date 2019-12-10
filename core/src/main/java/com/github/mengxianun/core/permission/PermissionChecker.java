package com.github.mengxianun.core.permission;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.PermissionException;
import com.github.mengxianun.core.item.SQLValue;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.parser.info.ConditionInfo;
import com.github.mengxianun.core.parser.info.FilterInfo;
import com.github.mengxianun.core.parser.info.JoinInfo;
import com.github.mengxianun.core.parser.info.RelationInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.parser.info.ValuesInfo;
import com.github.mengxianun.core.parser.info.WhereInfo;
import com.github.mengxianun.core.parser.info.extension.StatementValueConditionInfo;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.RequestKeyword;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public final class PermissionChecker {

	private static final Logger logger = LoggerFactory.getLogger(PermissionChecker.class);

	private PermissionChecker() {
		throw new AssertionError();
	}

	public static boolean check(SimpleInfo simpleInfo) {
		return checkWithResult(simpleInfo).pass();
	}

	public static PermissionCheckResult checkWithResult(SimpleInfo simpleInfo) {
		PermissionPolicy policy = App.getPermissionPolicy();
		if (policy == null || policy == PermissionPolicy.ALLOW_ALL) {
			return PermissionCheckResult.create(true, simpleInfo);
		}
		if (policy == PermissionPolicy.DENY_ALL) {
			return PermissionCheckResult.create(false, simpleInfo);
		}
		PermissionCheckResult tableCheckResult = checkTableWithResult(simpleInfo);
		PermissionCheckResult columnCheckResult = checkColumnWithResult(tableCheckResult.simpleInfo());
		return PermissionCheckResult.create(tableCheckResult.pass() && columnCheckResult.pass(),
				columnCheckResult.simpleInfo());
	}

	private static PermissionCheckResult checkTableWithResult(SimpleInfo simpleInfo) {
		PermissionPolicy policy = App.getPermissionPolicy();
		List<TablePermission> applyTablePermissions = new ArrayList<>();
		Action action = getAction(simpleInfo.operation());
		TableInfo primaryTableInfo = simpleInfo.table();
		List<TableInfo> joinTableInfos = simpleInfo.joins().stream().map(JoinInfo::tableInfo)
				.collect(Collectors.toList());
		List<TableInfo> actionTableInfos = new ArrayList<>();
		if (primaryTableInfo != null) {
			actionTableInfos.add(primaryTableInfo);
		}
		actionTableInfos.addAll(joinTableInfos);

		for (TableInfo tableInfo : actionTableInfos) {
			String source = tableInfo.source();
			String table = tableInfo.table();
			if (!App.hasTablePermissions(source, table)) {
				if (policy == PermissionPolicy.WEAK) {
					continue;
				}
				return PermissionCheckResult.create(false, simpleInfo);
			}

			List<TablePermission> tablePermissions = App.getTablePermissions(source, table);
			boolean check = false;
			boolean configured = false;
			for (TablePermission tablePermission : tablePermissions) {
				configured = true;
				Action permissionAction = tablePermission.action();
				if (action == permissionAction || permissionAction == Action.ALL) {
					check = true;
					applyTablePermissions.add(tablePermission);
					break;
				}
			}
			if (!check) {
				if (policy == PermissionPolicy.WEAK && !configured) {
					continue;
				}
				logger.warn("Table [{}.{}] has no [{}] permissions", source, table, action);
				return PermissionCheckResult.create(false, simpleInfo);
			}
		}
		simpleInfo = applyTableConditions(simpleInfo, applyTablePermissions);
		return PermissionCheckResult.create(true, simpleInfo);
	}

	public static SimpleInfo applyTableConditions(SimpleInfo simpleInfo, List<TablePermission> tablePermissions) {
		if (tablePermissions.isEmpty()) {
			return simpleInfo;
		}
		List<FilterInfo> newConditionFilters = new ArrayList<>();
		List<StatementValueConditionInfo> statementValueConditions = new ArrayList<>();
		List<RelationInfo> relations = new ArrayList<>();
		for (TablePermission tablePermission : tablePermissions) {
			List<ConnectorCondition> conditions = tablePermission.conditions();
			for (ConnectorCondition connectorCondition : conditions) {
				Connector connector = connectorCondition.connector();
				Condition condition = connectorCondition.condition();
				if (condition instanceof TableCondition) {
					TableCondition tableCondition = (TableCondition) condition;
					List<String> relationTablesPath = tableCondition.relationTablesPath();
					String source = tableCondition.source();
					if (Strings.isNullOrEmpty(source)) {
						source = App.getDefaultDataSource();
					}
					String table = tableCondition.table();
					String column = tableCondition.column();
					Object value = tableCondition.value();
					if (value != null && "$session".equalsIgnoreCase(value.toString())) { // session condition
						AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
						String userTable = authorizationInfo.getUserTable();
						Object userId = authorizationInfo.getUserId();
						if (userTable.equalsIgnoreCase(table)) {
							value = userId;
							FilterInfo filterInfo = FilterInfo.create(connector, ConditionInfo
									.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value,
											relationTablesPath));
							newConditionFilters.add(filterInfo);
						} else { // get statement value
							String conditionSql = getTableConditionSql(table, column);
							FilterInfo filterInfo = FilterInfo.create(connector, ConditionInfo.create(
									ColumnInfo.create(source, table, column, null), Operator.IN_SQL, conditionSql,
									relationTablesPath));

							newConditionFilters.add(filterInfo);
						}
					} else { // Specific conditions
						FilterInfo filterInfo = FilterInfo.create(connector, ConditionInfo
								.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value,
										relationTablesPath));
						newConditionFilters.add(filterInfo);
					}
					relations.addAll(tableCondition.relations());
				} else if (condition instanceof ExpressionCondition) {
					ExpressionCondition expressionCondition = (ExpressionCondition) condition;
					String expression = expressionCondition.expression();
					List<String> relationTablesPath = expressionCondition.relationTablesPath();
					ConditionInfo conditionInfo = new SimpleParser("").parseCondition(expression);

					/////////////////
					ColumnInfo columnInfo = conditionInfo.columnInfo();
					String source = columnInfo.source();
					String table = columnInfo.table();
					String column = columnInfo.column();
					Operator operator = conditionInfo.operator();
					Object value = conditionInfo.value();
					if (value != null && "$session".equalsIgnoreCase(value.toString())) { // session condition
						AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
						String userTable = authorizationInfo.getUserTable();
						Object userId = authorizationInfo.getUserId();
						if (userTable.equalsIgnoreCase(table)) {
							value = userId;
							FilterInfo filterInfo = FilterInfo.create(connector, ConditionInfo
									.create(ColumnInfo.create(source, table, column, null), operator, value,
											relationTablesPath));
							newConditionFilters.add(filterInfo);
						} else { // get statement value
							String conditionSql = getTableConditionSql(table, column, operator);
							value = SQLValue.create(conditionSql);

							FilterInfo filterInfo = FilterInfo.create(connector, ConditionInfo.create(
									ColumnInfo.create(source, table, column, null), operator, value,
									relationTablesPath));
							newConditionFilters.add(filterInfo);
						}
					} else {
						FilterInfo filterInfo = FilterInfo.create(connector,
								ConditionInfo.create(columnInfo, operator, value, relationTablesPath));
						newConditionFilters.add(filterInfo);
					}
					relations.addAll(expressionCondition.relations());
					/////////////////

					//					FilterInfo filterInfo = FilterInfo.create(connector, conditionInfo);
					//					newConditionFilters.add(filterInfo);
				}
			}
		}
		if (!newConditionFilters.isEmpty()) {
			List<FilterInfo> filters = simpleInfo.where().filters();
			FilterInfo filterInfo = FilterInfo.create(Connector.AND, null, newConditionFilters);
			filters = new ArrayList<>(filters);
			filters.add(filterInfo);
			//			List<FilterInfo> newFilters = Stream.concat(filters.stream(), newConditionFilters.stream())
			//					.collect(Collectors.toList());
			simpleInfo = simpleInfo.withWhere(WhereInfo.create(filters));
		}
		if (!statementValueConditions.isEmpty()) {
			simpleInfo = simpleInfo.withStatementValueConditions(statementValueConditions);
		}
		if (!relations.isEmpty()) {
			simpleInfo = simpleInfo.withRelations(relations);
		}
		return simpleInfo;
	}

	private static String getTableConditionSql(String table, String column) {
		AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
		String userTable = authorizationInfo.getUserTable();
		String userIdColumn = authorizationInfo.getUserIdColumn();
		Object userId = authorizationInfo.getUserId();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name().toLowerCase(), table);
		jsonObject.addProperty(RequestKeyword.FIELDS.lowerName(), column);
		jsonObject.addProperty(RequestKeyword.JOIN.lowerName(), userTable);
		jsonObject.addProperty(RequestKeyword.WHERE.lowerName(), userTable + "." + userIdColumn + "=" + userId);
		DataContext dataContext = App.getDefaultDataContext();
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

	private static String getTableConditionSql(String table, String column, Operator operator) {
		AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
		String userTable = authorizationInfo.getUserTable();
		String userIdColumn = authorizationInfo.getUserIdColumn();
		Object userId = authorizationInfo.getUserId();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name().toLowerCase(), table);

		switch (operator) {
		case LT:
		case LTE:
			column = "$min(" + table + "." + column + ")";
			break;
		case GT:
		case GTE:
			column = "$max(" + table + "." + column + ")";
			break;

		default:
			break;
		}
		jsonObject.addProperty(RequestKeyword.FIELDS.lowerName(), column);
		jsonObject.addProperty(RequestKeyword.JOIN.lowerName(), userTable);
		jsonObject.addProperty(RequestKeyword.WHERE.lowerName(), userTable + "." + userIdColumn + "=" + userId);
		DataContext dataContext = App.getDefaultDataContext();
		SimpleInfo simpleInfo = SimpleParser.parse(jsonObject);
		com.github.mengxianun.core.Action action = (com.github.mengxianun.core.Action) new CRUDActionParser(simpleInfo,
				dataContext).parse();
		action.build();
		try {
			return "(" + SQLParser.fill(action.getSql(), action.getParams().toArray()) + ")";
		} catch (SQLException e) {
			throw new DataException("Condition sql build fail");
		}
	}

	private static PermissionCheckResult checkColumnWithResult(SimpleInfo simpleInfo) {
		if (simpleInfo.operation().isQuery()) {
			return checkSelectColumnWithResult(simpleInfo);
		} else if (simpleInfo.operation() == Operation.INSERT || simpleInfo.operation() == Operation.UPDATE) {
			return checkUpdateColumnWithResult(simpleInfo);
		}
		return PermissionCheckResult.create(true, simpleInfo);
	}

	private static PermissionCheckResult checkSelectColumnWithResult(SimpleInfo simpleInfo) {
		Action action = getAction(simpleInfo.operation());
		List<ColumnInfo> columns = simpleInfo.columns();
		List<ColumnInfo> excludeColumns = new ArrayList<>();
		if (columns.isEmpty()) { // add exclude columns
			TableInfo primaryTableInfo = simpleInfo.table();
			List<TableInfo> joinTableInfos = simpleInfo.joins().stream().map(JoinInfo::tableInfo)
					.collect(Collectors.toList());
			List<TableInfo> actionTableInfos = new ArrayList<>();
			if (primaryTableInfo != null) {
				actionTableInfos.add(primaryTableInfo);
			}
			actionTableInfos.addAll(joinTableInfos);

			for (TableInfo tableInfo : actionTableInfos) {
				final String source = tableInfo.source();
				final String table = tableInfo.table();
				Table sourceTable = App.getTable(source, table);
				if (sourceTable == null) {
					continue;
				}
				for (String column : sourceTable.getColumnNames()) {
					boolean check = checkColumn(source, table, column, action);
					if (!check) {
						excludeColumns.add(ColumnInfo.create(source, table, column, null));
					}
				}
			}
		} else {
			for (ColumnInfo columnInfo : columns) {
				String source = columnInfo.source();
				if (Strings.isNullOrEmpty(source)) {
					source = App.getDefaultDataSource();
				}
				String table = columnInfo.table();
				// Default primary table
				// Check here to avoid null exceptions
				if (Strings.isNullOrEmpty(table)) {
					table = simpleInfo.table().table();
				}
				String column = columnInfo.column();
				if (!App.hasColumnPermissions(source, table, column)) {
					if (App.getPermissionPolicy() == PermissionPolicy.WEAK) {
						continue;
					}
					return PermissionCheckResult.create(false, simpleInfo);
				}
				boolean check = checkColumn(columnInfo.source(), columnInfo.table(), columnInfo.column(), action);
				if (!check) {
					excludeColumns.add(columnInfo);
				}
			}
		}
		simpleInfo = simpleInfo.withExcludeColumns(excludeColumns);
		return PermissionCheckResult.create(true, simpleInfo);
	}

	private static PermissionCheckResult checkUpdateColumnWithResult(SimpleInfo simpleInfo) {
		Action action = getAction(simpleInfo.operation());
		String source = simpleInfo.table().source();
		String table = simpleInfo.table().table();
		List<ColumnInfo> columns = new ArrayList<>();
		if (simpleInfo.operation() == Operation.INSERT) {
			ValuesInfo valuesInfo = simpleInfo.insertValues().get(0);
			for (Entry<String, Object> entry : valuesInfo.values().entrySet()) {
				String column = entry.getKey();
				columns.add(ColumnInfo.create(source, table, column, null));
			}
		} else if (simpleInfo.operation() == Operation.UPDATE) {
			ValuesInfo updateValues = simpleInfo.updateValues();
			for (Entry<String, Object> entry : updateValues.values().entrySet()) {
				String column = entry.getKey();
				columns.add(ColumnInfo.create(source, table, column, null));
			}
		}

		for (ColumnInfo columnInfo : columns) {
			String column = columnInfo.column();
			if (!App.hasColumnPermissions(source, table, column)) {
				if (App.getPermissionPolicy() == PermissionPolicy.WEAK) {
					continue;
				}
				return PermissionCheckResult.create(false, simpleInfo);
			}
			boolean check = checkColumn(columnInfo.source(), columnInfo.table(), columnInfo.column(), action);
			if (!check) {
				String message = String.format("Column [%s.%s.%s] has no [%s] permission", source, table, column,
						simpleInfo.operation());
				throw new PermissionException(message);
			}
		}
		return PermissionCheckResult.create(true, simpleInfo);
	}

	public static boolean checkColumn(String source, String table, String column, Action action) {
		boolean configured = App.hasTableColumnPermissions(source, table);
		boolean check = false;
		List<ColumnPermission> columnPermissions = App.getColumnPermissions(source, table, column);
		over: for (ColumnPermission columnPermission : columnPermissions) {
			Action columnAction = columnPermission.action();
			if (action == columnAction || columnAction == Action.ALL) {
				check = true;
				for (ConnectorCondition connectorCondition : columnPermission.conditions()) {
					Connector connector = connectorCondition.connector();
					Condition condition = connectorCondition.condition();
					if (condition instanceof ColumnCondition) {
						ColumnCondition columnCondition = (ColumnCondition) condition;
						Object value = columnCondition.value();
						// This condition must be a determination of the table, column, and value
						if (Strings.isNullOrEmpty(table) || Strings.isNullOrEmpty(column) || value == null) {
							continue;
						}
						if (value instanceof Integer) {
							value = Long.valueOf((Integer) value);
						}
						List<Object> values = getColumnConditionValues(table, column);
						if (!values.contains(value)) { // no permissions
							check = false;
							break over;
						}
					}
				}
				break;
			}
		}
		if (!check) {
			if (App.getPermissionPolicy() == PermissionPolicy.WEAK && !configured) {
				check = true;
			} else {
				logger.warn("Column [{}.{}.{}] has no [{}] permissions", source, table, column, action);
			}
		}
		return check;
	}

	/**
	 * 获取列权限的值
	 * 举例: 如果条件为{source: null, table: role, column: id, value: 1}
	 * 意思是: 只有角色ID为1的用户有权限
	 * 下列方法获取的是当前用户的角色ID集合,
	 * 如果这个ID集合包含ID为1的角色, 则说明当前用户包含该角色, 也就有有权限
	 * 
	 * @param table
	 * @param column
	 * @return
	 */
	private static List<Object> getColumnConditionValues(String table, String column) {
		AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
		String userTable = authorizationInfo.getUserTable();
		String userIdColumn = authorizationInfo.getUserIdColumn();
		Object userId = authorizationInfo.getUserId();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name().toLowerCase(), table);
		jsonObject.addProperty(RequestKeyword.FIELDS.lowerName(), column);
		jsonObject.addProperty(RequestKeyword.JOIN.lowerName(), userTable);
		jsonObject.addProperty(RequestKeyword.WHERE.lowerName(), userTable + "." + userIdColumn + "=" + userId);
		DataContext dataContext = App.getDefaultDataContext();
		SimpleInfo simpleInfo = SimpleParser.parse(jsonObject);
		com.github.mengxianun.core.Action action = (com.github.mengxianun.core.Action) new CRUDActionParser(simpleInfo,
				dataContext).parse();
		Summary summary = action.execute();

		Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
		List<Map<String, Object>> data = App.gson().fromJson(App.gson().toJson(summary.getData()), dataType);
		return data.parallelStream().map(e -> e.get(column)).collect(Collectors.toList());
	}

	private static Action getAction(Operation operation) {
		switch (operation) {
		case DETAIL:
		case SELECT:
		case SELECT_DISTINCT:
		case QUERY:
			return Action.SELECT;
		case INSERT:
			return Action.INSERT;
		case UPDATE:
			return Action.UPDATE;
		case DELETE:
			return Action.DELETE;

		default:
			break;
		}
		return Action.ALL;
	}

}

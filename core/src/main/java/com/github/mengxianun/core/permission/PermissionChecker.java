package com.github.mengxianun.core.permission;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.PermissionException;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.parser.info.ConditionInfo;
import com.github.mengxianun.core.parser.info.FilterInfo;
import com.github.mengxianun.core.parser.info.JoinInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.parser.info.ValuesInfo;
import com.github.mengxianun.core.parser.info.WhereInfo;
import com.github.mengxianun.core.parser.info.extension.StatementConditionInfo;
import com.github.mengxianun.core.parser.info.extension.StatementValueConditionInfo;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.RequestKeyword;
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
		List<Condition> permissionConditions = new ArrayList<>();
		String defaultSource = App.getDefaultDataSource();
		TableAction action = getTableAction(simpleInfo.operation());
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
			if (Strings.isNullOrEmpty(source)) {
				source = defaultSource;
			}
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
				TableAction permissionAction = tablePermission.action();
				if (action == permissionAction || permissionAction == TableAction.ALL) {
					check = true;
					permissionConditions.addAll(tablePermission.conditions());
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
		simpleInfo = applyTableConditions(simpleInfo, permissionConditions);
		return PermissionCheckResult.create(true, simpleInfo);
	}

	public static SimpleInfo applyTableConditions(SimpleInfo simpleInfo, List<Condition> conditions) {
		if (conditions.isEmpty()) {
			return simpleInfo;
		}
		List<FilterInfo> filters = simpleInfo.where().filters();
		List<FilterInfo> newConditionFilters = new ArrayList<>();
		List<StatementValueConditionInfo> statementValueConditions = new ArrayList<>();
		List<StatementConditionInfo> statementConditions = new ArrayList<>();
		for (Condition condition : conditions) {
			if (condition instanceof TableCondition) {
				TableCondition tableCondition = (TableCondition) condition;
				String source = tableCondition.source();
				if (Strings.isNullOrEmpty(source)) {
					source = App.getDefaultDataSource();
				}
				String table = tableCondition.table();
				if (Strings.isNullOrEmpty(table)) {
					continue;
				}
				String column = tableCondition.column();
				if (Strings.isNullOrEmpty(column)) {
					//////////////////
					// optimize
					//////////////////
					column = App.getDefaultDataContext().getTable(table).getPrimaryKeys().get(0).getName();
				}
				Object value = tableCondition.value();
				if (value == null) { // session condition
					AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
					String userTable = authorizationInfo.getUserTable();
					Object userId = authorizationInfo.getUserId();
					if (userTable.equalsIgnoreCase(table)) {
						value = userId;
						FilterInfo filterInfo = FilterInfo.create(ConditionInfo
								.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value));
						newConditionFilters.add(filterInfo);
					} else { // get statement value
						String conditionSql = getTableConditionSql(table, column);
						StatementValueConditionInfo statementValueConditionInfo = StatementValueConditionInfo
								.create(ColumnInfo.create(source, table, column, null), Operator.IN, conditionSql);
						statementValueConditions.add(statementValueConditionInfo);
					}
				} else { // Specific conditions
					FilterInfo filterInfo = FilterInfo.create(ConditionInfo
							.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value));
					newConditionFilters.add(filterInfo);
				}
			} else if (condition instanceof ExpressionCondition) {
				ExpressionCondition expressionCondition = (ExpressionCondition) condition;
				String expression = expressionCondition.expression();
				ConditionInfo conditionInfo = new SimpleParser("").parseCondition(expression);
				FilterInfo filterInfo = FilterInfo.create(conditionInfo);
				newConditionFilters.add(filterInfo);
			}
		}
		if (!newConditionFilters.isEmpty()) {
			List<FilterInfo> newFilters = Stream.concat(filters.stream(), newConditionFilters.stream())
					.collect(Collectors.toList());
			simpleInfo = simpleInfo.withWhere(WhereInfo.create(newFilters));
		}
		if (!statementValueConditions.isEmpty()) {
			simpleInfo = simpleInfo.withStatementValueConditions(statementValueConditions);
		}
		if (!statementConditions.isEmpty()) {
			simpleInfo = simpleInfo.withStatementConditions(statementConditions);
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

	private static PermissionCheckResult checkColumnWithResult(SimpleInfo simpleInfo) {
		if (simpleInfo.operation().isQuery()) {
			return checkSelectColumnWithResult(simpleInfo);
		} else if (simpleInfo.operation() == Operation.INSERT || simpleInfo.operation() == Operation.UPDATE) {
			return checkUpdateColumnWithResult(simpleInfo);
		}
		return PermissionCheckResult.create(true, simpleInfo);
	}

	private static PermissionCheckResult checkSelectColumnWithResult(SimpleInfo simpleInfo) {
		PermissionPolicy policy = App.getPermissionPolicy();
		ColumnAction action = getColumnAction(simpleInfo.operation());
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
				String tableInfoSource = tableInfo.source();
				final String source = Strings.isNullOrEmpty(tableInfoSource) ? App.getDefaultDataSource()
						: tableInfoSource;
				final String table = tableInfo.table();
				Map<String, List<ColumnPermission>> columnPermissionsInTable = App.getColumnPermissionsInTable(source,
						table);
				for (Entry<String, List<ColumnPermission>> entry : columnPermissionsInTable.entrySet()) {
					String column = entry.getKey();
					List<ColumnPermission> columnPermissions = entry.getValue();

					ColumnInfo columnInfo = ColumnInfo.create(source, table, column, null);
					boolean check = checkColumn(columnInfo, action, columnPermissions, policy);
					if (!check) {
						excludeColumns.add(columnInfo);
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
				String column = columnInfo.column();
				if (!App.hasColumnPermissions(source, table, column)) {
					if (policy == PermissionPolicy.WEAK) {
						continue;
					}
					return PermissionCheckResult.create(false, simpleInfo);
				}
				List<ColumnPermission> columnPermissions = App.getColumnPermissions(source, table, column);
				boolean check = checkColumn(columnInfo, action, columnPermissions, policy);
				if (!check) {
					excludeColumns.add(columnInfo);
				}
			}
		}
		simpleInfo = simpleInfo.withExcludeColumns(excludeColumns);
		return PermissionCheckResult.create(true, simpleInfo);
	}

	private static PermissionCheckResult checkUpdateColumnWithResult(SimpleInfo simpleInfo) {
		PermissionPolicy policy = App.getPermissionPolicy();
		ColumnAction action = getColumnAction(simpleInfo.operation());
		String source = simpleInfo.table().source();
		if (Strings.isNullOrEmpty(source)) {
			source = App.getDefaultDataSource();
		}
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
				if (policy == PermissionPolicy.WEAK) {
					continue;
				}
				return PermissionCheckResult.create(false, simpleInfo);
			}
			List<ColumnPermission> columnPermissions = App.getColumnPermissions(source, table, column);
			boolean check = checkColumn(columnInfo, action, columnPermissions, policy);
			if (!check) {
				String message = String.format("Column [%s.%s.%s] has no [%s] permission", source, table, column,
						simpleInfo.operation());
				throw new PermissionException(message);
			}
		}
		return PermissionCheckResult.create(true, simpleInfo);
	}

	private static boolean checkColumn(ColumnInfo columnInfo, ColumnAction action,
			List<ColumnPermission> columnPermissions,
			PermissionPolicy policy) {
		String source = columnInfo.source();
		String table = columnInfo.table();
		String column = columnInfo.column();
		boolean configured = !columnPermissions.isEmpty();
		boolean check = false;
		over: for (ColumnPermission columnPermission : columnPermissions) {
			ColumnAction columnAction = columnPermission.action();
			if (action == columnAction || columnAction == ColumnAction.ALL) {
				check = true;
				for (Condition condition : columnPermission.conditions()) {
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
			if (policy == PermissionPolicy.WEAK && !configured) {
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

	private static TableAction getTableAction(Operation operation) {
		switch (operation) {
		case DETAIL:
		case SELECT:
		case SELECT_DISTINCT:
		case QUERY:
			return TableAction.QUERY;
		case INSERT:
			return TableAction.ADD;
		case UPDATE:
			return TableAction.UPDATE;
		case DELETE:
			return TableAction.DELETE;

		default:
			break;
		}
		return TableAction.ALL;
	}

	private static ColumnAction getColumnAction(Operation operation) {
		switch (operation) {
		case DETAIL:
		case SELECT:
		case SELECT_DISTINCT:
		case QUERY:
			return ColumnAction.READ;
		case INSERT:
			return ColumnAction.INSERT;
		case UPDATE:
			return ColumnAction.UPDATE;
		case DELETE:
			return ColumnAction.WRITE;

		default:
			break;
		}
		return ColumnAction.ALL;
	}

}

package com.github.mengxianun.core.permission;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.SQLParser;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.action.CRUDActionParser;
import com.github.mengxianun.core.parser.info.ColumnInfo;
import com.github.mengxianun.core.parser.info.ConditionInfo;
import com.github.mengxianun.core.parser.info.FilterInfo;
import com.github.mengxianun.core.parser.info.JoinInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.parser.info.WhereInfo;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Operator;
import com.github.mengxianun.core.request.RequestKeyword;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;

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
			return PermissionCheckResult.create(true, Collections.emptyList());
		}
		if (policy == PermissionPolicy.DENY_ALL) {
			return PermissionCheckResult.create(false, Collections.emptyList());
		}
		List<Condition> permissionConditions = new ArrayList<>();
		String defaultSource = App.getDefaultDataSource();
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
			if (Strings.isNullOrEmpty(source)) {
				source = defaultSource;
			}
			if (!App.hasTablePermissions(source, table)) {
				if (policy == PermissionPolicy.WEAK) {
					continue;
				}
				return PermissionCheckResult.create(false, Collections.emptyList());
			}

			List<TablePermission> tablePermissions = App.getTablePermissions(defaultSource, table);
			boolean check = false;
			boolean configured = false;
			for (TablePermission tablePermission : tablePermissions) {
				String permissionSource = tablePermission.source();
				if (Strings.isNullOrEmpty(permissionSource)) {
					permissionSource = defaultSource;
				}
				String permissionTable = tablePermission.table();
				Action permissionAction = tablePermission.action();
				if (source.equalsIgnoreCase(permissionSource) && table.equalsIgnoreCase(permissionTable)) {
					configured = true;
					if (action == permissionAction || permissionAction == Action.ALL) {
						check = true;
						//						applyConditions(simpleInfo, tablePermission.conditions());
						permissionConditions.addAll(tablePermission.conditions());
						break;
					}
				}
			}
			if (!check) {
				if (policy == PermissionPolicy.WEAK && !configured) {
					continue;
				}
				logger.warn("Table [{}.{}] has no [{}] permissions", source, table, action);
				return PermissionCheckResult.create(false, Collections.emptyList());
			}
		}
		return PermissionCheckResult.create(true, permissionConditions);
	}

	public static SimpleInfo applyConditions(SimpleInfo simpleInfo, List<Condition> conditions) {
		if (conditions.isEmpty()) {
			return simpleInfo;
		}
		//		List<JoinInfo> joins = simpleInfo.joins();
		List<FilterInfo> filters = simpleInfo.where().filters();
		List<FilterInfo> newFilters = new ArrayList<>(filters);
		for (Condition condition : conditions) {
			if (condition instanceof TableCondition) {
				TableCondition tableCondition = (TableCondition) condition;
				String source = tableCondition.source();
				if (Strings.isNullOrEmpty(source)) {
					source = App.getDefaultDataSource();
				}
				String table = tableCondition.table();
				String column = tableCondition.column();
				Object value = tableCondition.value();
				//			joins.add(JoinInfo.create(JoinType.LEFT, TableInfo.create(source, table, null)));
				FilterInfo filterInfo;
				if (value == null) { // session condition
					AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
					String userTable = authorizationInfo.getUserTable();
					String userIdColumn = authorizationInfo.getUserIdColumn();
					Object userId = authorizationInfo.getUserIdSupplier().get();
					if (userTable.equalsIgnoreCase(table)) {
						value = userId;
						filterInfo = FilterInfo.create(ConditionInfo
								.create(ColumnInfo.create(source, table, column, null), Operator.EQUAL, value));
					} else { // get value
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty(Operation.SELECT.name(), table);
						jsonObject.addProperty(RequestKeyword.FIELDS.name(), column);
						jsonObject.addProperty(RequestKeyword.JOIN.name(), userTable);
						jsonObject.addProperty(RequestKeyword.WHERE.name(),
								userTable + "." + userIdColumn + "=" + userId);
						String conditionSql = getConditionSql(table, column);
						filterInfo = FilterInfo.create(ConditionInfo.create(
								ColumnInfo.create(source, userTable, userIdColumn, null), Operator.IN, conditionSql));
					}
				} else {
					filterInfo = FilterInfo.create(ConditionInfo.create(ColumnInfo.create(source, table, column, null),
							Operator.EQUAL, value));
				}
				newFilters.add(filterInfo);
			} else if (condition instanceof ExpressionCondition) {
				//
			}
		}
		return simpleInfo.withWhere(WhereInfo.create(newFilters));
	}

	private static String getConditionSql(String table, String column) {
		AuthorizationInfo authorizationInfo = App.getAuthorizationInfo();
		String userTable = authorizationInfo.getUserTable();
		String userIdColumn = authorizationInfo.getUserIdColumn();
		Object userId = authorizationInfo.getUserIdSupplier().get();
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty(Operation.SELECT.name(), table);
		jsonObject.addProperty(RequestKeyword.FIELDS.name(), column);
		jsonObject.addProperty(RequestKeyword.JOIN.name(), userTable);
		jsonObject.addProperty(RequestKeyword.WHERE.name(), userTable + "." + userIdColumn + "=" + userId);
		DataContext dataContext = App.getDefaultDataContext();
		SimpleInfo simpleInfo = SimpleParser.parse(jsonObject);
		com.github.mengxianun.core.Action action = (com.github.mengxianun.core.Action) new CRUDActionParser(simpleInfo,
				dataContext).parse();
		try {
			return SQLParser.fill(action.getSql(), action.getParams());
		} catch (SQLException e) {
			throw new DataException("Condition sql build fail");
		}
	}

	private static Action getAction(Operation operation) {
		switch (operation) {
		case DETAIL:
		case SELECT:
		case SELECT_DISTINCT:
		case QUERY:
			return Action.QUERY;
		case INSERT:
			return Action.ADD;
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

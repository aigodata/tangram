package com.github.mengxianun.jdbc;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.h2.tools.RunScript;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.DefaultTranslator;
import com.github.mengxianun.core.permission.ColumnAction;
import com.github.mengxianun.core.permission.ColumnCondition;
import com.github.mengxianun.core.permission.ColumnPermission;
import com.github.mengxianun.core.permission.ConnectorCondition;
import com.github.mengxianun.core.permission.ExpressionCondition;
import com.github.mengxianun.core.permission.SimpleAuthorizationInfo;
import com.github.mengxianun.core.permission.TableAction;
import com.github.mengxianun.core.permission.TableCondition;
import com.github.mengxianun.core.permission.TablePermission;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestSupport {
	
	static final Logger LOG = Logger.getLogger(TestSupport.class.getName());
	
	public static final String DB_DRIVER_CLASS_NAME = "org.h2.Driver";
	public static final String DB_URL = "jdbc:h2:~/test";
	public static final String DB_USERNAME = "test";
	public static final String DB_PASSWORD = "123456";
	public static final String DATABASE_INIT_SCRIPT = "init.sql";
	
	private static final String TEST_CONFIG_FILE = "test.json";
	public static final DefaultTranslator translator;

	static {
		// Initialize test data source
		String scriptPath = TestSupport.class.getClassLoader().getResource(DATABASE_INIT_SCRIPT).toString();
		try {
			RunScript.execute(DB_URL, DB_USERNAME, DB_PASSWORD, scriptPath, Charset.defaultCharset(), false);
			LOG.log(Level.INFO, "Database {0} init successful", DB_URL);
		} catch (SQLException e) {
			LOG.log(Level.SEVERE, "Script run failed", e);
		}
		// Create Translator
		translator = new DefaultTranslator(TEST_CONFIG_FILE);
		translator.addFactory(new JdbcDataContextFactory());
		translator.reInit();
		initAuthorizationInfo();
	}

	static void initAuthorizationInfo() {
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo(null, "permission_user", "id",
				() -> getUserId(),
				() -> geTablePermissions(), () -> getColumnPermissions());
		App.setAuthorizationInfo(simpleAuthorizationInfo);
	}

	static Object getUserId() {
		return 1;
	}

	static List<TablePermission> geTablePermissions() {
		List<TablePermission> tablePermissions = new ArrayList<>();
		tablePermissions.add(TablePermission.create(null, "permission_all_table"));
		tablePermissions.add(TablePermission.create("ds", "permission_query_table", TableAction.QUERY));
		tablePermissions.add(TablePermission.create(null, "permission_add_table", TableAction.from("add")));
		tablePermissions.add(TablePermission.create(null, "permission_update_table", TableAction.UPDATE));
		tablePermissions.add(TablePermission.create(null, "permission_delete_table", TableAction.from("delete")));
		// session user condition
		List<ConnectorCondition> userTableConditions = Lists.newArrayList(ConnectorCondition.create(TableCondition.create("permission_user")));
		tablePermissions
				.add(TablePermission.builder().table("permission_condition_user_table").action(TableAction.QUERY)
				.conditions(userTableConditions).build());
		// session role condition
		List<ConnectorCondition> roleTableConditions = Lists.newArrayList(ConnectorCondition.create(TableCondition.create("permission_role")));
		tablePermissions.add(TablePermission.builder().table("permission_condition_role_table")
				.action(TableAction.QUERY)
				.conditions(roleTableConditions).build());
		// expression condition
		List<ConnectorCondition> expressionConditions = Lists.newArrayList(ConnectorCondition.create(ExpressionCondition.create("id>1")));
		tablePermissions.add(TablePermission.builder().table("permission_condition_expression_table")
				.action(TableAction.QUERY).conditions(expressionConditions).build());
		// complex condition
		List<ConnectorCondition> complexConditions = Lists.newArrayList(
				ConnectorCondition.create(TableCondition.create("permission_role", "id")),
				ConnectorCondition.create(ExpressionCondition.create("id<5")));
		tablePermissions.add(TablePermission.builder().table("permission_condition_user_table2")
				.action(TableAction.QUERY).conditions(complexConditions).build());
		return tablePermissions;
	}

	static List<ColumnPermission> getColumnPermissions() {
		List<ColumnPermission> columnPermissions = new ArrayList<>();
		columnPermissions.add(ColumnPermission.create("permission_column_table", "all_column", ColumnAction.ALL));
		columnPermissions.add(ColumnPermission.create("permission_column_table", "select_column", ColumnAction.READ));
		columnPermissions.add(ColumnPermission.create("permission_column_table", "insert_column", ColumnAction.INSERT));
		columnPermissions.add(ColumnPermission.create("permission_column_table", "update_column", ColumnAction.UPDATE));
		columnPermissions.add(ColumnPermission.create("permission_column_table", "delete_column", ColumnAction.WRITE));
		columnPermissions.add(ColumnPermission.create("permission_column_join_table", "name", ColumnAction.UPDATE));
		columnPermissions
				.add(ColumnPermission.create(null, "permission_column_condition_table", "COLUMN_USER_1",
						ColumnAction.READ,
						Lists.newArrayList(ConnectorCondition.create(ColumnCondition.create("user", "id", 1)))));
		columnPermissions.add(ColumnPermission.create(null, "permission_column_condition_table", "COLUMN_USER_1",
				ColumnAction.INSERT,
				Lists.newArrayList(ConnectorCondition.create(ColumnCondition.create("user", "id", 1)))));
		columnPermissions.add(ColumnPermission.create(null, "permission_column_condition_table", "COLUMN_USER_2",
				ColumnAction.READ,
				Lists.newArrayList(ConnectorCondition.create(ColumnCondition.create("user", "id", 2)))));
		columnPermissions.add(ColumnPermission.create(null, "permission_column_condition_table", "COLUMN_USER_2",
				ColumnAction.INSERT,
				Lists.newArrayList(ConnectorCondition.create(ColumnCondition.create("user", "id", 2)))));
		return columnPermissions;
	}

	String readJson(String jsonFile) {
		URL url = Resources.getResource(jsonFile);
		try {
			return Resources.toString(url, StandardCharsets.UTF_8);
		} catch (IOException e) {
			LOG.warning(e.getMessage());
			return "";
		}
	}

	DataResultSet run(String jsonFile) {
		return runJson(readJson(jsonFile));
	}

	DataResultSet runJson(String json) {
		DataResultSet dataResultSet = translator.translate(json);
		LOG.info("-----------------Json-----------------");
		LOG.info(json);
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		LOG.info("-----------------Result-----------------");
		LOG.info(gson.toJson(dataResultSet.getData()));
		return dataResultSet;
	}

}

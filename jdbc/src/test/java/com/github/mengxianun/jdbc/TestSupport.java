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
import com.github.mengxianun.core.permission.Action;
import com.github.mengxianun.core.permission.SimpleAuthorizationInfo;
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
		SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo("user", "id", () -> getUserId(),
				() -> geTablePermissions());
		App.setAuthorizationInfo(simpleAuthorizationInfo);
	}

	static Object getUserId() {
		return 1;
	}

	static List<TablePermission> geTablePermissions() {
		List<TablePermission> tablePermissions = new ArrayList<>();
		tablePermissions.add(TablePermission.create(null, "permission_all_table"));
		tablePermissions.add(TablePermission.create("ds", "permission_query_table", Action.QUERY));
		tablePermissions.add(TablePermission.create(null, "permission_add_table", Action.from("add")));
		tablePermissions.add(TablePermission.create(null, "permission_update_table", Action.UPDATE));
		tablePermissions.add(TablePermission.create(null, "permission_delete_table", Action.from("delete")));
		// session user condition
		List<TableCondition> userTableConditions = Lists.newArrayList(TableCondition.create("user", "id"));
		tablePermissions.add(TablePermission.builder().table("permission_condition_user_table").action(Action.QUERY)
				.conditions(userTableConditions).build());
		return tablePermissions;
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

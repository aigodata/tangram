package com.github.mengxianun.jdbc;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.h2.tools.RunScript;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.DefaultTranslator;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class TestSupport {
	
	static final Logger LOG = Logger.getLogger(TestSupport.class.getName());
	
	public static final String DB_DRIVER_CLASS_NAME = "org.h2.Driver";
	public static final String DB_URL = "jdbc:h2:~/test";
	public static final String DB_USERNAME = "test";
	public static final String DB_PASSWORD = "123456";
	public static final String DATABASE_INIT_SCRIPT = "test.sql";
	
	private static final String TEST_CONFIG_FILE = "test.json";
	public static final DefaultTranslator translator;

	static {
		// Initialize test database
		String scriptPath = TestSupport.class.getClassLoader().getResource(DATABASE_INIT_SCRIPT).toString();
		try {
			//			Class.forName(DB_DRIVER_CLASS_NAME);
			RunScript.execute(DB_URL, DB_USERNAME, DB_PASSWORD, scriptPath, Charset.defaultCharset(), false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Create Translator
		translator = new DefaultTranslator(TEST_CONFIG_FILE);
		translator.addFactory(new JdbcDataContextFactory());
		translator.reInit();
	}

	String readJson(String jsonFile) {
		URL url = Resources.getResource(jsonFile);
		try {
			return Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
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
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		LOG.info("-----------------Result-----------------");
		LOG.info(gson.toJson(dataResultSet));
		return dataResultSet;
	}

}

package com.github.mengxianun.elasticsearch;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeAll;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.DefaultTranslator;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class TestSupport {

	static final Logger LOG = Logger.getLogger(TestSupport.class.getName());

	public static DefaultTranslator translator = new DefaultTranslator();
	public static final String DB_URL = "jdbc:elasticsearch://192.168.0.75:9300/";
	public static final String DATASOURCE_NAME = "ds";

	public static boolean databaseCreated = false;

	public static void createDataContext() {
		JsonObject dataSourceJsonObject = new JsonObject();
		dataSourceJsonObject.addProperty("url", DB_URL);
		ElasticsearchDataContext dataContext = new ElasticsearchDataContextFactory().create(dataSourceJsonObject);
		translator.registerDataContext(DATASOURCE_NAME, dataContext);
	}

	@BeforeAll
	static void initAll() {
		if (databaseCreated) {
			return;
		}
		createDataContext();
		databaseCreated = true;
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

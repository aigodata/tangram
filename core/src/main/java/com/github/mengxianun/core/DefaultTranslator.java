package com.github.mengxianun.core;

import java.net.URL;

import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;
import com.google.gson.JsonElement;

public class DefaultTranslator extends AbstractTranslator {

	public DefaultTranslator() {
		this(App.Config.getString(ConfigAttributes.CONFIG_FILE));
	}

	public DefaultTranslator(String configFile) {
		super.init(configFile);
	}

	public DefaultTranslator(URL configFileURL) {
		super.init(configFileURL);
	}

	@Override
	public DataResultSet translate(String json) {

		Executor executor = App.getInjector().getInstance(Executor.class);
		JsonElement result = executor.execute(json);

		return new DefaultDataResultSet(0L, result);

	}

}

package com.github.mengxianun.core;

import java.net.URL;
import java.time.Duration;

import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;
import com.google.common.base.Stopwatch;
import com.google.gson.JsonElement;

public class DefaultTranslator extends AbstractTranslator {

	public DefaultTranslator() {
		this(App.Config.getString(ConfigAttributes.CONFIG_FILE));
	}

	public DefaultTranslator(String configFile) {
		super.init(configFile);
		App.createInjector();
	}

	public DefaultTranslator(URL configFileURL) {
		super.init(configFileURL);
	}

	@Override
	public DataResultSet translate(String json) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Executor executor = App.getInjector().getInstance(Executor.class);
		JsonElement result = executor.execute(json);

		Duration duration = stopwatch.stop().elapsed();
		return new DefaultDataResultSet(duration.toMillis(), result);

	}

}

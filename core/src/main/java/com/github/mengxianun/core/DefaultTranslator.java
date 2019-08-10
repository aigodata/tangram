package com.github.mengxianun.core;

import java.net.URL;

import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;

public class DefaultTranslator extends AbstractTranslator {

	public DefaultTranslator() {
		this(App.Config.getString(GlobalConfig.CONFIG_FILE));
	}

	public DefaultTranslator(String configFile) {
		super.init(configFile);
	}

	public DefaultTranslator(URL configFileURL) {
		super.init(configFileURL);
	}

	@Override
	protected DataResultSet execute(DataContext dataContext, Action action) {
		Summary summary = dataContext.execute(action);
		return new DefaultDataResultSet(summary.getData());
	}

}

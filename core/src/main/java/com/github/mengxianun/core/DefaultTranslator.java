package com.github.mengxianun.core;

import java.net.URL;

import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.executor.Executor;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;

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
	protected DataResultSet execute(String json) {
		Object result = null;
		Executor executor = App.factory().createExecutor(App.currentDataContext());
		DataResult dataResult = executor.execute(json);
		if (dataResult.isQuery()) {
			result = dataResult.getDataSet();
		} else if (dataResult.isUpdate()) {
			result = dataResult.getUpdateSummary();
		} else {
			result = dataResult.getData();
		}

		return new DefaultDataResultSet(result);
	}

}

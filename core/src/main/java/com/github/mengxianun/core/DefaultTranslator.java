package com.github.mengxianun.core;

import java.net.URL;

import com.github.mengxianun.core.attributes.ConfigAttributes;
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
	protected DataResultSet execute(Action action) {
		Object result = null;
		DataResult dataResult = App.currentDataContext().execute(action);

		if (dataResult.isQuery()) {
			result = dataResult.getDataSet();
		} else if (dataResult.isUpdate()) {
			result = dataResult.getUpdateSummary().getSummary();
		} else {
			result = dataResult.getData();
		}

		return new DefaultDataResultSet(result);
	}

}

package com.github.mengxianun.core;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.DataSourceConfig;
import com.github.mengxianun.core.config.GlobalConfig;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.JsonDataException;
import com.github.mengxianun.core.parser.ActionParser;
import com.github.mengxianun.core.parser.ParserFactory;
import com.github.mengxianun.core.parser.SimpleParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class AbstractTranslator implements Translator {

	private static final Logger logger = LoggerFactory.getLogger(AbstractTranslator.class);
	protected final Map<String, DataContextFactory> factories = new HashMap<>();

	protected void init(String configFile) {
		init(convertToURL(configFile));
	}

	protected void init(URL configFileURL) {
		if (configFileURL != null) {
			readConfig(configFileURL);
		}
		try {
			parseAllTableConfig(App.Config.getString(GlobalConfig.TABLE_CONFIG_PATH));
		} catch (IOException e) {
			logger.error("Table config file parse error", e);
		}
	}

	protected URL convertToURL(String configFile) {
		try {
			URL configFileURL = Resources.getResource(configFile);
			App.Config.set(GlobalConfig.CONFIG_FILE, configFile);
			return configFileURL;
		} catch (Exception e) {
			logger.error(String.format("Config file [%s] parse error", configFile), e);
		}
		return null;
	}

	protected void readConfig(URL configFileURL) {
		try {
			String configurationFileContent = Resources.toString(configFileURL, StandardCharsets.UTF_8);
			JsonObject configurationJsonObject = new JsonParser().parse(configurationFileContent).getAsJsonObject();
			// 覆盖默认配置
			for (Entry<String, JsonElement> entry : configurationJsonObject.entrySet()) {
				App.Config.set(entry.getKey(), entry.getValue());
			}
			createDataContexts();
		} catch (IOException e) {
			logger.error(String.format("Config file [%s] parse error", configFileURL), e);
		}
	}

	protected void createDataContexts() {
		discoverFromClasspath();

		JsonObject dataSourcesJsonObject = App.Config.getJsonObject(GlobalConfig.DATASOURCES);
		for (Entry<String, JsonElement> entry : dataSourcesJsonObject.entrySet()) {
			String dataSourceName = entry.getKey();
			JsonObject dataSourceJsonObject = dataSourcesJsonObject.getAsJsonObject(dataSourceName);
			
			if (App.hasDataContext(dataSourceName)) {
				logger.info("Data source [{}] already exists", dataSourceName);
				continue;
			}
			String type = parseDataContextType(dataSourceJsonObject);
			if (type == null || !factories.containsKey(type)) {
				String message = String.format("Data source [%s] type [%s] is not supported", dataSourceName, type);
				logger.error(message, new DataException());
				continue;
			}
			DataContextFactory dataContextFactory = factories.get(type);
			if (dataContextFactory != null) {
				dataSourceJsonObject.remove(DataSourceConfig.TYPE);
				DataContext dataContext = dataContextFactory.create(dataSourceJsonObject);
				addDataContext(dataSourceName, dataContext);
				logger.info("Create data source [{}] successfully", dataSourceName);
			} else {
				logger.warn("Create data source [{}] failed, Could not find DataContextFactory with type [{}]",
						dataSourceName, type);
			}
		}

		App.initDefaultDataSource();
	}

	/**
	 * 解析数据源类型, 如果指定了 type 属性, 以 指定的 type 为准. 如果没有指定 type 属性, 则从url属性中解析数据源类型
	 * 
	 * @param dataSourceJsonObject
	 * @return DataContext Type
	 */
	protected String parseDataContextType(JsonObject dataSourceJsonObject) {
		String type = null;
		if (dataSourceJsonObject.has(DataSourceConfig.TYPE)) {
			type = dataSourceJsonObject.get(DataSourceConfig.TYPE).getAsString();
		} else {
			if (dataSourceJsonObject.has(DataSourceConfig.URL)) {
				String url = dataSourceJsonObject.get(DataSourceConfig.URL).getAsString();
				for (String factoryType : factories.keySet()) {
					if (url.contains(factoryType)) {
						type = factoryType;
						break;
					}
				}
			}
		}
		return type;
	}

	protected void parseAllTableConfig(String tableConfigPath) throws IOException {
		Map<String, DataContext> dataContexts = App.getDataContexts();
		for (Map.Entry<String, DataContext> entry : dataContexts.entrySet()) {
			String dataContextName = entry.getKey();
			DataContext dataContext = entry.getValue();
			String sourceTableConfigDir = tableConfigPath + File.separator + dataContextName;
			ConfigHelper.parseSourceTableConfig(sourceTableConfigDir, dataContext);
		}
	}

	protected void addDataContext(String name, DataContext dataContext) {
		if (App.hasDataContext(name)) {
			throw new DataException(String.format("DataContext [%s] already exists", name));
		}
		App.addDataContext(name, dataContext);
		if (!App.Config.has(GlobalConfig.DEFAULT_DATASOURCE)
				|| Strings.isNullOrEmpty(App.Config.getString(GlobalConfig.DEFAULT_DATASOURCE))) {
			String defaultDataSourceName = App.getDataContexts().keySet().iterator().next();
			App.Config.set(GlobalConfig.DEFAULT_DATASOURCE, defaultDataSourceName);
		}
	}

	protected void discoverFromClasspath() {
		final ServiceLoader<DataContextFactory> serviceLoader = ServiceLoader.load(DataContextFactory.class);
		for (DataContextFactory factory : serviceLoader) {
			addFactory(factory);
		}
	}

	public void addFactory(DataContextFactory factory) {
		factories.put(factory.getType(), factory);
	}

	public void reInit() {
		init(App.Config.getString(GlobalConfig.CONFIG_FILE));
	}

	@Override
	public DataResultSet translate(String json) {
		logger.debug("Request: \n{}", json);

		// Sets the context of the current thread
		SimpleInfo simpleInfo = SimpleParser.parse(json);
		String sourceName = null;
		if (simpleInfo.source() != null) {
			sourceName = simpleInfo.source().source();
		} else if (simpleInfo.table() != null) {
			sourceName = simpleInfo.table().source();
		}
		if (Strings.isNullOrEmpty(sourceName)) {
			sourceName = App.getDefaultDataSource();
		}
		if (!App.hasDataContext(sourceName)) {
			throw new JsonDataException(ResultStatus.DATASOURCE_NOT_EXIST.fill(sourceName));
		}
		DataContext dataContext = App.getDataContext(sourceName);
		App.setCurrentDataContext(dataContext);

		// Stopwatch
		Stopwatch stopwatch = Stopwatch.createStarted();

		// Permission valid
		checkPermission(simpleInfo);

		ActionParser actionParser = ParserFactory.getActionParser(simpleInfo, dataContext);
		NewAction newAction = actionParser.parse();

		Summary summary = newAction.execute();
		DataResultSet dataResultSet = new DefaultDataResultSet(summary);

		//		DataResultSet dataResultSet = execute(dataContext, action);

		// Done
		Duration duration = stopwatch.stop().elapsed();

		logger.debug("Operation completed in {} milliseconds", duration.toMillis());

		// Cleans up the context of the current thread
		App.cleanup();

		return dataResultSet;
	}

	private void checkPermission(SimpleInfo simpleInfo) {
		//		HashBasedTable<String, String, List<TablePermission>> tablePermissions = App.getTablePermissions();
		//		PermissionChecker.check(action, tablePermissions);
	}

	protected abstract DataResultSet execute(DataContext dataContext, Action action);

	/**
	 * Release resources
	 */
	@PreDestroy
	protected void destroy() {
		logger.info("Destroy all DataContext...");
		for (Map.Entry<String, DataContext> entry : App.getDataContexts().entrySet()) {
			String name = entry.getKey();
			DataContext dataContext = entry.getValue();
			dataContext.destroy();
			logger.info("DataContext [{}] destroyed", name);

		}
		logger.info("All DataContext is already destroyed");
	}

}

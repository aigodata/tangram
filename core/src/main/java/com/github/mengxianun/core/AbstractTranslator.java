package com.github.mengxianun.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.attributes.AssociationType;
import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.attributes.DataSourceAttributes;
import com.github.mengxianun.core.attributes.TableConfigAttributes;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.interceptor.TranslatorInterceptor;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public abstract class AbstractTranslator implements Translator {

	private static final Logger logger = LoggerFactory.getLogger(AbstractTranslator.class);

	// 默认配置文件名
	protected static final String DEFAULT_CONFIG_FILE = "air.json";
	// 默认数据表配置路径
	protected static final String DEFAULT_TABLE_CONFIG_PATH = "tables";
	// 全局配置
	protected static final JsonObject configuration = new JsonObject();
	protected final Map<String, DataContext> dataContexts = new HashMap<>();
	private final List<DataContextFactory> factories = new ArrayList<>();

	static {
		// 初始化默认属性
		configuration.addProperty(ConfigAttributes.CONFIG_FILE, DEFAULT_CONFIG_FILE);
		configuration.add(ConfigAttributes.DATASOURCES, JsonNull.INSTANCE);
		configuration.addProperty(ConfigAttributes.UPSERT, false);
		configuration.addProperty(ConfigAttributes.NATIVE, false);
		configuration.addProperty(ConfigAttributes.LOG, false);
		configuration.addProperty(ConfigAttributes.DEFAULT_DATASOURCE, "");
		configuration.addProperty(ConfigAttributes.TABLE_CONFIG_PATH, DEFAULT_TABLE_CONFIG_PATH);
		configuration.add(ConfigAttributes.TABLE_CONFIG, JsonNull.INSTANCE);
		// 预处理开关
		configuration.add(ConfigAttributes.PRE_HANDLER, JsonNull.INSTANCE);
		// 权限控制
		configuration.add(ConfigAttributes.AUTH_CONTROL, JsonNull.INSTANCE);
	}

	protected void init(String configFile) {
		init(convertToURL(configFile));
	}

	protected void init(URL configFileURL) {
		if (configFileURL != null) {
			readConfig(configFileURL);
		}
		readTablesConfig(configuration.getAsJsonPrimitive(ConfigAttributes.TABLE_CONFIG_PATH).getAsString());
	}

	private URL convertToURL(String configFile) {
		try {
			URL configFileURL = Resources.getResource(configFile);
			configuration.addProperty(ConfigAttributes.CONFIG_FILE, configFile);
			return configFileURL;
		} catch (Exception e) {
			logger.error(String.format("config file [%s] parse error", configFile), e);
		}
		return null;
	}

	private void readConfig(URL configFileURL) {
		try {
			String configurationFileContent = Resources.toString(configFileURL, Charsets.UTF_8);
			JsonObject configurationJsonObject = new JsonParser().parse(configurationFileContent).getAsJsonObject();
			// 覆盖默认配置
			for (Entry<String, JsonElement> entry : configurationJsonObject.entrySet()) {
				configuration.add(entry.getKey(), entry.getValue());
			}
			createDataContext();
		} catch (IOException e) {
			logger.error(String.format("config file [%s] parse error", configFileURL), e);
		}
	}

	private void createDataContext() {
		discoverFromClasspath();

		JsonObject dataSourcesJsonObject = configuration.getAsJsonObject(ConfigAttributes.DATASOURCES);
		// 是否配置了默认数据源, 在没有配置默认数据源的情况下, 将第一个数据源设置为默认数据源
		if (!dataSourcesJsonObject.has(ConfigAttributes.DEFAULT_DATASOURCE)) {
			String defaultDataSourceName = dataSourcesJsonObject.keySet().iterator().next();
			configuration.addProperty(ConfigAttributes.DEFAULT_DATASOURCE, defaultDataSourceName);
		}

		for (Entry<String, JsonElement> entry : dataSourcesJsonObject.entrySet()) {
			String dataSourceName = entry.getKey();
			JsonObject dataSourceJsonObject = dataSourcesJsonObject.getAsJsonObject(dataSourceName);
			String type = parseDataContextType(dataSourceName, dataSourceJsonObject);
			for (DataContextFactory dataContextFactory : factories) {
				if (dataContextFactory.getType().equals(type)) {
					dataSourceJsonObject.remove(ConfigAttributes.DATASOURCE_TYPE);
					DataContext dataContext = dataContextFactory.create(dataSourceJsonObject);
					addDataContext(dataSourceName, dataContext);
					break;
				}
			}
		}
	}

	/**
	 * 解析数据源类型, 如果指定了 type 属性, 以 指定的 type 为准. 如果没有指定 type 属性, 则从url属性中解析数据源类型
	 * 
	 * @param dataSourceName
	 * @param dataSourceJsonObject
	 * @return
	 */
	private String parseDataContextType(String dataSourceName, JsonObject dataSourceJsonObject) {
		if (dataSourceJsonObject.has(ConfigAttributes.DATASOURCE_TYPE)) {
			return dataSourceJsonObject.get(ConfigAttributes.DATASOURCE_TYPE).getAsString();
		} else {
			if (dataSourceJsonObject.has(DataSourceAttributes.URL)) {
				String url = dataSourceJsonObject.get(DataSourceAttributes.URL).getAsString();
				for (DataContextFactory dataContextFactory : factories) {
					if (url.contains(dataContextFactory.getType())) {
						return dataContextFactory.getType();
					}
				}
			}
		}
		throw new DataException(String.format("Data source [%s] lacks the type attribute", dataSourceName));
	}

	/**
	 * 读取所有数据库表配置文件, 结构
	 * <li>tablePath
	 * <li>- db1
	 * <li>-- table1.json
	 * <li>-- table2.json
	 * <li>- db2
	 * <li>-- table1.json
	 * <li>-- table2.json
	 * 
	 * @param tablesConfigPath
	 */
	private void readTablesConfig(String tablesConfigPath) {
		URL tablesConfigURL = Thread.currentThread().getContextClassLoader().getResource(tablesConfigPath);
		if (tablesConfigURL == null) {
			return;
		}
		URI uri;
		try {
			uri = tablesConfigURL.toURI();
		} catch (URISyntaxException e) {
			throw new DataException(e);
		}
		if (uri.getScheme().equals("jar")) {
			try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap())) {
				Path tableConfigPath = fileSystem.getPath("/WEB-INF/classes/" + tablesConfigPath);
				traverseTablesConfig(tableConfigPath);
			} catch (IOException e) {
				throw new DataException(e);
			}
		} else {
			Path tableConfigPath = Paths.get(new File(uri).getPath());
			traverseTablesConfig(tableConfigPath);
		}

	}

	/**
	 * 遍历数据库表存储路径
	 * 
	 * @param tableConfigPath
	 */
	private void traverseTablesConfig(Path tableConfigPath) {
		try (Stream<Path> stream = Files.walk(tableConfigPath, 2)) { // 这里循环2层, 由结构决定
			stream.filter(Files::isRegularFile).forEach(path -> {
				Path parentPath = path.getParent();
				try {
					// 根目录下的表配置文件, 默认为默认数据源的表配置
					if (Files.isSameFile(parentPath, tableConfigPath)) {
						DataContext dataContext = getDefaultDataContext();
						readTableConfig(path, dataContext);
						return;
					} else {
						String parentFileName = parentPath.getFileName().toString();
						if (!dataContexts.containsKey(parentFileName)) { // 文件名不是数据源
							return;
						}
						DataContext dataContext = getDataContext(parentFileName);
						readTableConfig(path, dataContext);
					}
				} catch (IOException e) {
					throw new DataException(e);
				}
			});
		} catch (IOException e) {
			throw new DataException(e);
		}
	}

	/**
	 * 读取数据表配置文件, 文件名为表名
	 * 
	 * @param path
	 *            数据表配置文件路径
	 * @param dataContext
	 */
	private void readTableConfig(Path path, DataContext dataContext) {
		if (dataContext == null) {
			return;
		}
		String fileName = path.getFileName().toString();
		String tableName = fileName.substring(0, fileName.lastIndexOf("."));
		try {
			String content = Resources.toString(path.toUri().toURL(), Charsets.UTF_8);
			JsonElement jsonElement = new JsonParser().parse(content);
			JsonObject tableConfig = jsonElement.getAsJsonObject();
			Table table = dataContext.getTable(tableName);
			if (table == null) {
				logger.warn("Table [{}] from [{}] does not exist", tableName, fileName);
				return;
			}
			table.setConfig(tableConfig);
			if (tableConfig.has(TableConfigAttributes.COLUMNS)) {
				JsonObject columnsConfig = tableConfig.get(TableConfigAttributes.COLUMNS).getAsJsonObject();
				for (String columnName : columnsConfig.keySet()) {
					Column column = dataContext.getColumn(tableName, columnName);
					if (column != null) {
						JsonObject columnConfig = columnsConfig.get(columnName).getAsJsonObject();
						column.setConfig(columnConfig);
						// 添加 Relationship
						if (columnConfig.has(TableConfigAttributes.COLUMN_ASSOCIATION)) {
							JsonObject associationConfig = columnConfig
									.getAsJsonObject(TableConfigAttributes.COLUMN_ASSOCIATION);
							String targetTableName = associationConfig
									.getAsJsonPrimitive(TableConfigAttributes.ASSOCIATION_TARGET_TABLE).getAsString();
							String targetColumnName = associationConfig
									.getAsJsonPrimitive(TableConfigAttributes.ASSOCIATION_TARGET_COLUMN).getAsString();
							AssociationType associationType = associationConfig
									.has(TableConfigAttributes.ASSOCIATION_TYPE)
											? AssociationType.from(associationConfig
													.getAsJsonPrimitive(TableConfigAttributes.ASSOCIATION_TYPE)
													.getAsString())
											: AssociationType.ONE_TO_ONE;
							Column targetColumn = dataContext.getColumn(targetTableName, targetColumnName);
							// 添加主表对外表的关联
							dataContext.addRelationship(column, targetColumn, associationType);
							// 添加外表对主表的关联
							dataContext.addRelationship(targetColumn, column,
									associationType.reverse());
						}
					}
				}
			}
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			throw new DataException(String.format("Parsing table config file [%s] failed", path), e);
		}
	}

	public void addDataContext(String name, DataContext dataContext) {
		if (dataContexts.containsKey(name)) {
			throw new DataException(String.format("DataContext [%s] already exists", name));
		}
		dataContexts.put(name, dataContext);
		if (!configuration.has(ConfigAttributes.DEFAULT_DATASOURCE)
				|| Strings.isNullOrEmpty(configuration.get(ConfigAttributes.DEFAULT_DATASOURCE).getAsString())) {
			String defaultDataSourceName = dataContexts.keySet().iterator().next();
			configuration.addProperty(ConfigAttributes.DEFAULT_DATASOURCE, defaultDataSourceName);
		}
	}

	@Override
	public void registerDataContext(String name, DataContext dataContext) {
		addDataContext(name, dataContext);
		init(configuration.getAsJsonPrimitive(ConfigAttributes.CONFIG_FILE).getAsString());
	}

	public void discoverFromClasspath() {
		final ServiceLoader<DataContextFactory> serviceLoader = ServiceLoader.load(DataContextFactory.class);
		for (DataContextFactory factory : serviceLoader) {
			addFactory(factory);
		}
	}

	public void addFactory(DataContextFactory factory) {
		factories.add(factory);
	}

	public DataContext getDataContext(String dataSourceName) {
		return dataContexts.get(dataSourceName);
	}

	public DataContext getDefaultDataContext() {
		return getDataContext(getDefaultDataSource());
	}

	public String getDefaultDataSource() {
		return configuration.getAsJsonPrimitive(ConfigAttributes.DEFAULT_DATASOURCE).getAsString();
	}

	@Override
	public List<String> getDataSourceNames() {
		return Lists.newArrayList(dataContexts.keySet());
	}

	@Override
	public String getDataSourceName(String type) {
		for (String sourceName : dataContexts.keySet()) {
			DataContext dataContext = dataContexts.get(sourceName);
			if (dataContext.getDialect().getType().equals(type)) {
				return sourceName;
			}
		}
		return null;
	}

	/**
	 * 预处理，在取得数据之前的处理，比如：检验表的操作权限
	 *
	 * @param parser
	 */
	protected void preHandler(com.github.mengxianun.core.JsonParser parser) {
		ServiceLoader<TranslatorInterceptor> translatorInterceptors = ServiceLoader.load(TranslatorInterceptor.class);
		if (translatorInterceptors != null) {
			for (TranslatorInterceptor interceptor : translatorInterceptors) {
				interceptor.preHandler(parser, configuration);
			}
		}
	}

	/**
	 * 释放资源
	 */
	@PreDestroy
	public void cleanup() {
		logger.info("Close Air DataContext");
		for (String dataContextName : dataContexts.keySet()) {
			DataContext dataContext = dataContexts.get(dataContextName);
			try {
				dataContext.destroy();
				logger.info("DataContext [{}] destroyed", dataContextName);
			} catch (Throwable e) {
				logger.error(String.format("DataContext [%s] destroy failed", dataContextName), e);
			}

		}
	}

}

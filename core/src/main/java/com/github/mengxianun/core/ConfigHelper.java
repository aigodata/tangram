package com.github.mengxianun.core;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.io.Resources;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public final class ConfigHelper {

	private static final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

	private ConfigHelper() {
		throw new IllegalStateException("Utility class");
	}

	public static void parseSourceTableConfig(String tableConfigDir, DataContext dataContext) throws IOException {
		URL tablesConfigURL = Thread.currentThread().getContextClassLoader().getResource(tableConfigDir);
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
				Path sourceTableConfigDirPath = fileSystem.getPath("/WEB-INF/classes/" + tableConfigDir);
				parseSourceTableConfig(sourceTableConfigDirPath, dataContext);
			}
		} else {
			Path sourceTableConfigDirPath = Paths.get(new File(uri).getPath());
			parseSourceTableConfig(sourceTableConfigDirPath, dataContext);
		}
	}

	public static void parseSourceTableConfig(Path sourceTableConfigDir, DataContext dataContext) throws IOException {
		try (Stream<Path> stream = Files.walk(sourceTableConfigDir, 1)) {
			stream.filter(Files::isRegularFile).forEach(path -> {
				parseTableConfigFile(path, dataContext);
			});
		}
	}

	/**
	 * 读取数据表配置文件, 文件名为表名
	 * 
	 * @param path
	 *            数据表配置文件路径
	 * @param dataContext
	 */
	public static void parseTableConfigFile(Path path, DataContext dataContext) {
		if (dataContext == null) {
			return;
		}
		String fileName = path.getFileName().toString();
		String tableName = fileName.substring(0, fileName.lastIndexOf("."));
		try {
			String content = Resources.toString(path.toUri().toURL(), StandardCharsets.UTF_8);
			JsonElement jsonElement = new JsonParser().parse(content);
			JsonObject tableConfig = jsonElement.getAsJsonObject();
			Table table = dataContext.getTable(tableName);
			if (table == null) {
				logger.warn("Table [{}] from [{}] does not exist", tableName, fileName);
				return;
			}
			table.setConfig(tableConfig);
			if (tableConfig.has(TableConfig.COLUMNS)) {
				JsonObject columnsConfig = tableConfig.get(TableConfig.COLUMNS).getAsJsonObject();
				for (String columnName : columnsConfig.keySet()) {
					Column column = dataContext.getColumn(tableName, columnName);
					if (column != null) {
						JsonObject columnConfig = columnsConfig.get(columnName).getAsJsonObject();
						column.setConfig(columnConfig);
						// 添加 Relationship
						if (columnConfig.has(TableConfig.COLUMN_ASSOCIATION)) {
							JsonObject associationConfig = columnConfig.getAsJsonObject(TableConfig.COLUMN_ASSOCIATION);
							String targetTableName = associationConfig
									.getAsJsonPrimitive(TableConfig.ASSOCIATION_TARGET_TABLE).getAsString();
							String targetColumnName = associationConfig
									.getAsJsonPrimitive(TableConfig.ASSOCIATION_TARGET_COLUMN).getAsString();
							AssociationType associationType = associationConfig.has(TableConfig.ASSOCIATION_TYPE)
									? AssociationType.from(associationConfig
											.getAsJsonPrimitive(TableConfig.ASSOCIATION_TYPE).getAsString())
									: AssociationType.ONE_TO_ONE;
							Column targetColumn = dataContext.getColumn(targetTableName, targetColumnName);
							// 添加主外表的关联
							dataContext.addRelationship(column, targetColumn, associationType);
						}
					}
				}
			}
		} catch (JsonIOException | JsonSyntaxException | IOException e) {
			throw new DataException(String.format("Parsing table config file [%s] failed", path), e);
		}
	}

}

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
import java.util.Arrays;
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

	private static FileSystem fileSystem;

	private ConfigHelper() {
		throw new AssertionError();
	}

	public static void parseSourceTableConfig(String tableConfigDir, DataContext dataContext)
			throws IOException {
		Path tableConfigPath = getSourceTableConfigPath(tableConfigDir);
		if (!tableConfigPath.toFile().exists()) {
			return;
		}
		parseSourceTableConfig(tableConfigPath, dataContext);
		if (fileSystem != null && fileSystem.isOpen()) {
			fileSystem.close();
		}
	}

	public static Path getSourceTableConfigPath(String tableConfigDir) throws IOException {
		Path tableConfigPath = getPathFromClasspath(tableConfigDir);
		if (tableConfigPath == null) {
			tableConfigPath = getPathFromFileSystem(tableConfigDir);
		}
		return tableConfigPath;
	}

	public static Path getPathFromClasspath(String pathString) throws IOException {
		Path path = null;
		URL url = null;
		try {
			url = Thread.currentThread().getContextClassLoader().getResource(pathString);
		} catch (Exception ignored) {
			return null;
		}
		if (url == null) {
			return null;
		}
		URI uri;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			throw new DataException(e);
		}
		if (uri.getScheme().equals("jar")) {
			fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
			String[] pathParts = url.toString().split("!");
			String fileSystemPath = String.join("", Arrays.copyOfRange(pathParts, 1, pathParts.length));
			path = fileSystem.getPath(fileSystemPath);
		} else {
			path = Paths.get(new File(uri).getPath());
		}
		return path;
	}

	public static Path getPathFromFileSystem(String pathString) {
		Path path = Paths.get(pathString);
		if (!path.isAbsolute()) {
			String parentDir = System.getProperty("user.dir");
			String realPath = parentDir + File.separator + pathString;
			path = Paths.get(realPath);
		}
		return path;
	}

	public static void parseSourceTableConfig(Path sourceTableConfigDir, DataContext dataContext) throws IOException {
		try (Stream<Path> stream = Files.walk(sourceTableConfigDir, 1)) {
			stream.filter(Files::isRegularFile).forEach(path -> {
				parseTableConfigFile(path, dataContext);
			});
		}
	}

	/**
	 * read table config file, filename is table name
	 * 
	 * @param path
	 *            table config file path
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
					if (column == null) {
						logger.warn("Column [{}.{}] from [{}] does not exist", tableName, columnName, fileName);
						continue;
					}
					JsonObject columnConfig = columnsConfig.get(columnName).getAsJsonObject();
					column.setConfig(columnConfig);
					// Relationship
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
						if (targetColumn == null) {
							logger.warn("Column [{}.{}] from [{}] does not exist", targetTableName, targetColumnName,
									fileName);
						} else {
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

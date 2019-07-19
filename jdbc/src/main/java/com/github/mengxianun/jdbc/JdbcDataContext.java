package com.github.mengxianun.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.mengxianun.core.AbstractDataContext;
import com.github.mengxianun.core.Atom;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.data.DataSet;
import com.github.mengxianun.core.data.update.DefaultUpdateSummary;
import com.github.mengxianun.core.data.update.InsertSummary;
import com.github.mengxianun.core.data.update.UpdateSummary;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.schema.ColumnType;
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultColumnType;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.jdbc.dialect.JdbcDialectFactory;

public class JdbcDataContext extends AbstractDataContext {

	private static final Logger logger = LoggerFactory.getLogger(JdbcDataContext.class);
	// 当前线程操作的数据库连接
	private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();
	// 当前线程操作是否自动关闭连接
	public static final ThreadLocal<Boolean> closeConnection = new ThreadLocal<>();

	protected final DataSource dataSource;
	//
	protected final QueryRunner runner;

	public JdbcDataContext(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("DataSource cannot be null");
		}
		this.dataSource = dataSource;
		this.dialect = JdbcDialectFactory.getDialect(dataSource);
		this.runner = new QueryRunner(dataSource);
		closeConnection.set(true);
		initMetadata();
	}

	@Override
	public void initMetadata() {
		List<Schema> schemas = new ArrayList<>();
		metadata.setSchemas(schemas);
		// source.add(metadata.SCHEMAS, schemas);
		String defaultCatalogName = null;
		String defaultSchemaName = null;
		// 获取 Catalog 和 Schema
		DruidDataSource druidDataSource = (DruidDataSource) dataSource;
		String url = druidDataSource.getUrl();
		String username = druidDataSource.getUsername();
		String password = druidDataSource.getPassword();
		try (Connection connection = DriverManager.getConnection(url, username, password)) {
			defaultCatalogName = connection.getCatalog();
			defaultSchemaName = connection.getSchema();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
		}

		try (final Connection connection = getConnection()) {
			DatabaseMetaData databasemetadata = connection.getMetaData();
			// String databaseProductName = databasemetadata.getDatabaseProductName();
			// String databaseProductVersion = databasemetadata.getDatabaseProductVersion();
			// String url = databasemetadata.getURL();
			String identifierQuoteString = databasemetadata.getIdentifierQuoteString();
			metadata.setIdentifierQuoteString(identifierQuoteString);
			metadata.setDefaultSchemaName(defaultCatalogName);

			// schema metadata
			ResultSet catalogsResultSet = databasemetadata.getCatalogs();
			while (catalogsResultSet.next()) {
				String catalogName = catalogsResultSet.getString(1);
				if ("information_schema".equals(catalogName)) {
					continue;
				}
				schemas.add(new DefaultSchema(catalogName));
			}

			// table metadata
			DefaultSchema defaultSchema = (DefaultSchema) metadata.getSchema(defaultCatalogName);
			ResultSet tablesResultSet = databasemetadata.getTables(defaultCatalogName, defaultSchemaName, "%", null);
			while (tablesResultSet.next()) {
				// String tableCatalog = tablesResultSet.getString(1);
				// String tableSchema = tablesResultSet.getString(2);
				String tableName = tablesResultSet.getString(3);
				// String tableType = tablesResultSet.getString(4);
				String remarks = tablesResultSet.getString(5);
				defaultSchema.addTable(new DefaultTable(tableName, defaultSchema, remarks));
			}

			// column metadata
			ResultSet columnsResultSet = databasemetadata.getColumns(defaultCatalogName, defaultSchemaName, "%", null);
			while (columnsResultSet.next()) {
				// String columnCatalog = columnsResultSet.getString(1);
				// String columnSchema = columnsResultSet.getString(2);
				String columnTable = columnsResultSet.getString(3);
				String columnName = columnsResultSet.getString(4);
				String columnDataType = columnsResultSet.getString(5);
				String columnTypeName = columnsResultSet.getString(6);
				Integer columnSize = columnsResultSet.getInt(7);
				Boolean columnNullable = columnsResultSet.getBoolean(11);
				String columnRemarks = columnsResultSet.getString(12);
				// Boolean isAutoincrement = columnsResultSet.getBoolean(23);

				DefaultTable table = (DefaultTable) metadata.getTable(defaultCatalogName, columnTable);
				ColumnType columnType = new DefaultColumnType(Integer.parseInt(columnDataType), columnTypeName);
				table.addColumn(
						new DefaultColumn(table, columnType, columnName, columnNullable, columnRemarks, columnSize));
			}

		} catch (SQLException e) {
			throw new JdbcDataException(ResultStatus.DATASOURCE_EXCEPTION, e.getMessage());
		}
	}

	public void startTransaction() throws SQLException {
		Connection conn = threadLocalConnection.get();
		if (conn == null) {
			conn = getConnection();
		}
		threadLocalConnection.set(conn);
		conn.setAutoCommit(false);
		closeConnection.set(false);
		logger.debug("Start new transaction.");
	}

	public Connection getConnection() throws SQLException {
		Connection conn = threadLocalConnection.get();
		if (conn == null) {
			return dataSource.getConnection();
		} else {
			return conn;
		}
	}

	public void commit() throws SQLException {
		Connection conn = threadLocalConnection.get();
		if (conn != null) {
			conn.commit();
			logger.debug("Transaction commit.");
		}
	}

	public void rollback() throws SQLException {
		Connection conn = threadLocalConnection.get();
		if (conn != null) {
			conn.rollback();
			logger.debug("Transaction rollback.");
		}
	}

	public void close() throws SQLException {
		Connection conn = threadLocalConnection.get();
		if (conn != null) {
			try {
				conn.close();
				logger.debug("Transaction close.");
			} finally {
				threadLocalConnection.remove();
				closeConnection.set(true);
			}
		}
	}

	/**
	 * 指定一组事务操作
	 * 
	 * @param atoms
	 * @throws SQLException
	 */
	@Override
	public void trans(Atom... atoms) {
		if (null == atoms) {
			return;
		}
		try {
			startTransaction();
			for (Atom atom : atoms) {
				atom.run();
			}
			commit();
		} catch (SQLException e) {
			try {
				rollback();
			} catch (SQLException e1) {
				throw new JdbcDataException("Transaction rollback failed.");
			}
		} finally {
			try {
				close();
			} catch (SQLException e) {
				logger.error("Transaction close failed.", e);
			}
		}
	}

	@Override
	public DataResult executeNative(String statement) {
		return executeSql(statement);
	}

	@Override
	protected DataSet query(String sql, Object... params) {
		try {
			List<Object[]> values = runner.query(sql, new ArrayListHandler(), params);
			return new JdbcDataSet(null, values);
		} catch (SQLException e) {
			Throwable realReasion = e;
			SQLException nextException = e.getNextException();
			if (nextException != null && nextException.getCause() != null) {
				realReasion = nextException.getCause();
			}
			logger.error(ResultStatus.DATASOURCE_SQL_FAILED.message(), realReasion);
			throw new JdbcDataException(ResultStatus.DATASOURCE_SQL_FAILED, realReasion.getMessage());
		}

	}

	@Override
	protected UpdateSummary insert(String sql, Object... params) {
		try {
			List<Map<String, Object>> insertContents = runner.insert(sql, new MapListHandler(), params);
			return new InsertSummary(insertContents);
		} catch (SQLException e) {
			Throwable realReasion = e;
			SQLException nextException = e.getNextException();
			if (nextException != null && nextException.getCause() != null) {
				realReasion = nextException.getCause();
			}
			logger.error(ResultStatus.DATASOURCE_SQL_FAILED.message(), realReasion);
			throw new JdbcDataException(ResultStatus.DATASOURCE_SQL_FAILED, realReasion.getMessage());
		}
	}

	@Override
	protected UpdateSummary update(String sql, Object... params) {
		try {
			int updateCount = runner.update(sql, params);
			return new DefaultUpdateSummary(updateCount);
		} catch (SQLException e) {
			Throwable realReasion = e;
			SQLException nextException = e.getNextException();
			if (nextException != null && nextException.getCause() != null) {
				realReasion = nextException.getCause();
			}
			logger.error(ResultStatus.DATASOURCE_SQL_FAILED.message(), realReasion);
			throw new JdbcDataException(ResultStatus.DATASOURCE_SQL_FAILED, realReasion.getMessage());
		}
	}

}

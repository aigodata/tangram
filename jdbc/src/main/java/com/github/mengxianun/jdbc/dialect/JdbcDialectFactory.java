package com.github.mengxianun.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.DefaultDialect;
import com.github.mengxianun.core.Dialect;
import com.github.mengxianun.core.DialectFactory;

public class JdbcDialectFactory extends DialectFactory {

	private static final Logger logger = LoggerFactory.getLogger(JdbcDialectFactory.class);

	/**
	 * 通过DataSource判断数据库类型
	 * 
	 * @param dataSource
	 *            数据源
	 * @return Dialect
	 */
	public static Dialect getDialect(DataSource dataSource) {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			String url = metaData.getURL();
			return getDialect(url);
		} catch (Exception e) {
			logger.error("Failure to acquire dialect", e);
			return new DefaultDialect();
		}
	}

	/**
	 * 通过url判断数据库类型
	 * 
	 * @param url
	 *            数据库连接字符串
	 * @return 方言实例
	 */
	public static Dialect getDialect(String url) {
		for (Entry<String, Class<? extends Dialect>> entry : dialects.entrySet()) {
			String dbName = entry.getKey();
			if (url.indexOf(dbName) != -1) {
				try {
					return entry.getValue().newInstance();
				} catch (Exception e) {
					// 忽略
				}
			}
		}
		return new DefaultDialect();
	}

}

package com.github.mengxianun.core;

import java.util.List;

/**
 * 翻译器, 将 JSON 翻译为 DATA
 * 
 * @author mengxiangyun
 *
 */
public interface Translator {

	/**
	 * 将 JSON 翻译为对象返回
	 * 
	 * @param json
	 *            JSON 请求字符串
	 * @return Result
	 */
	public DataResultSet translate(String json);

	/**
	 * 将 JSON 翻译为对象返回, 同时添加过滤条件
	 * 
	 * @param json
	 * @param filterExpressions
	 * @return Result
	 */
	public DataResultSet translate(String json, String... filterExpressions);

	/**
	 * 注册 DataContext, 重新读取配置文件
	 * 
	 * @param name
	 * @param dataContext
	 */
	public void registerDataContext(String name, DataContext dataContext);

	/**
	 * 获取所有配置的数据源名称
	 * 
	 * @return 所有配置的数据源名称
	 */
	public List<String> getDataSourceNames();

	/**
	 * 获取指定类型的数据源名称
	 * 
	 * @param type
	 *            数据源类型. 例: mysql, elasticsearch
	 * @return 指定类型的数据源名称
	 */
	public String getDataSourceName(String type);

}

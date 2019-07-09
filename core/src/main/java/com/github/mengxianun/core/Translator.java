package com.github.mengxianun.core;

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

}

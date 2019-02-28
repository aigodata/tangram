package com.github.mengxianun.core.interceptor;

import com.github.mengxianun.core.JsonParser;
import com.google.gson.JsonObject;

public interface TranslatorInterceptor {

	/**
	 * 预处理
	 *
	 * @param parser
	 * 			解析过的请求参数
	 * @param properties
	 * 			配置文件
	 */
	void preHandler(JsonParser parser, JsonObject properties);
}

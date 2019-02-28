package com.github.mengxianun.core;

import com.github.mengxianun.core.utils.Utils;
import com.google.gson.*;

import java.io.*;
import java.util.*;

public abstract class AbstractResultConverter implements ResultConverter {

	//private static final Logger logger = LoggerFactory.getLogger(AbstractResultConverter.class);

	protected Map<String, Object> headers = null;
	protected Map<String, Object> config = null;

	protected List<Map<String, Object>> data;

	protected String getDisplayName(String key) {
		return this.headers.get(key).toString();
	}

	@Override
	public InputStream execute() throws Exception {
		//TODO 可以加AOP处理
		//logger.debug(this.getClass().getName() + " execute()");
		return export();
	}

	/**
	 * read the config and data of export file
	 */
	protected void dataInit(Map<String, Object> properties, Map<String, Object> header, JsonElement data) {
		this.config = properties;
		this.headers = header;
		this.data = formatData(data);
	}
	protected List<Map<String, Object>> formatData(JsonElement jsonElement) {

		List<Map<String, Object>> list = new ArrayList<>();
// TODO 老孟说以后会有分页，要留着这个if
//		if (jsonElement.containsKey(Structure.LIMIT)) { // 分页
//			list = ((JSONObject) jsonResult).getArray(PageResult.ATTRIBUTE.DATA).toMapList();
//		} else {
			list = Utils.json2List(jsonElement);
//		}
		return list;
	}
	protected static String replaceParams(String str, String key, String value) {
		return Utils.replaceParams(str, key, value);
	}
	protected abstract InputStream export() throws Exception;
}

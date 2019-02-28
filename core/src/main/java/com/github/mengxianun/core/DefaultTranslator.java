package com.github.mengxianun.core;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.attributes.ResultAttributes;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.exception.PreHandlerException;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.json.JsonAttributes;
import com.github.mengxianun.core.resutset.DefaultDataResultSet;
import com.github.mengxianun.core.resutset.FailDataResultSet;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DefaultTranslator extends AbstractTranslator {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTranslator.class);

	public DefaultTranslator() {
		this(configuration.getAsJsonPrimitive(ConfigAttributes.CONFIG_FILE).getAsString());
	}

	public DefaultTranslator(String configFile) {
		super.init(configFile);
	}

	public DefaultTranslator(URL configFileURL) {
		super.init(configFileURL);
	}

	@Override
	public DataResultSet translate(String json) {
		return translate(json, new String[] {});
	}
	@Override
	public DataResultSet translate(String json, String... filterExpressions) {

		long start = System.currentTimeMillis();

		try {
			// TODO step1. json解析处理
			JsonParser jsonParser = jsonParser(json);

			// TODO step2. 预处理（所有实现preHandler接口的预处理都会执行）
			super.preHandler(jsonParser);

			// TODO step3. 取数据
			JsonElement result = resultSet(jsonParser, filterExpressions);

			// TODO step4. 可以加后续处理
			long end = System.currentTimeMillis();

			// TODO step5. 结果封装
			return new DefaultDataResultSet(end - start, result);

		} catch (DataException e) {
			logger.error(e.getMessage(), e.getCause());
			return new FailDataResultSet(e.getCode(), e.getMessage());
		} catch (PreHandlerException e) {
			logger.error(e.getMessage(), e.getCause());
			return new FailDataResultSet(e.getCode(), e.getMessage());
		} catch (JsonSyntaxException e) {
			logger.error(e.getMessage(), e);
			return new FailDataResultSet(ResultStatus.JSON_FORMAT_ERROR);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new FailDataResultSet(ResultStatus.SYSTEM_ERROR.code(),
					ResultStatus.SYSTEM_ERROR.fill(e.getMessage()).message());
		}
	}

	/**
	 * json解析，把参数字符串解析成json对象并进行封装
	 *
	 * @param json
	 * @return
	 * @throws JsonSyntaxException
	 */
	private JsonParser jsonParser(String json) throws JsonSyntaxException {
		JsonParser jsonParser;
		try {
			JsonObject jsonData = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
			jsonParser = new JsonParser(jsonData, this);
			jsonParser.parse();
		} catch (Exception e) {
			throw e;
		}
		return jsonParser;
	}

	/**
	 * 取得数据
	 *
	 * @param jsonParser
	 * @param filterExpressions
	 * @return
	 */
	private JsonElement resultSet(JsonParser jsonParser, String... filterExpressions) {

		JsonElement result = null;

//			JsonObject jsonData = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
//			JsonParser jsonParser = new JsonParser(jsonData, this);
//			jsonParser.parse();
		// -------------------------
		// 添加额外过滤条件, 待优化
		// -------------------------
		if (filterExpressions != null && filterExpressions.length > 0) {
			Arrays.asList(filterExpressions).forEach(jsonParser::addFilter);
		}
		if (jsonParser.isStruct()) {
			TableItem tableItem = jsonParser.getAction().getTableItems().get(0);
			Table table = tableItem.getTable();
			result = new Gson().toJsonTree(table);
		} else if (jsonParser.isTransaction()) {
			JsonObject jsonData = jsonParser.getJsonData();
			JsonArray transactionArray = jsonData.getAsJsonArray(JsonAttributes.TRANSACTION);
			List<Action> actions = new ArrayList<>();
			DataContext dataContext = null;
			for (int i = 0; i < transactionArray.size(); i++) {
				JsonObject innerJsonData = transactionArray.get(i).getAsJsonObject();
				JsonParser innerJsonParser = new JsonParser(innerJsonData, this);
				innerJsonParser.parse();
				Action action = innerJsonParser.getAction();
				action.build();
				actions.add(action);
				// 将第一个Json操作的数据源作为整个事务数据源. 暂时不支持跨数据源事务
				if (i == 0) {
					dataContext = innerJsonParser.getDataContext();
				}
			}
			if (dataContext != null) {
				result = dataContext.action(actions.toArray(new Action[] {}));
			}
		} else if (jsonParser.isNative()) {
			TableItem tableItem = jsonParser.getAction().getTableItems().get(0);
			Table table = tableItem.getTable();
			result = jsonParser.getDataContext().executeNative(table, jsonParser.getNativeContent());
		} else if (jsonParser.isTemplate()) {
			// to do
		} else if (jsonParser.isResultFile()) {
			// to do
		} else {
			Action action = jsonParser.getAction();
			action.build();
			result = jsonParser.getDataContext().action(action);
			result = new DataRenderer().render(result, action);
			if (action.isLimit()) {
				LimitItem limitItem = action.getLimitItem();
				long start = limitItem.getStart();
				long end = limitItem.getEnd();
				JsonElement countElement = jsonParser.getDataContext().action(action.count());
				JsonObject countObject = countElement.getAsJsonObject();
				String countKey = countObject.has(ResultAttributes.COUNT) ? ResultAttributes.COUNT
						: ResultAttributes.COUNT.toUpperCase();
				long count = countObject.get(countKey).getAsLong();
				JsonObject pageResult = new JsonObject();
				pageResult.addProperty(ResultAttributes.START, start);
				pageResult.addProperty(ResultAttributes.END, end);
				pageResult.addProperty(ResultAttributes.TOTAL, count);
				pageResult.add(ResultAttributes.DATA, result);
				return pageResult;
			}
		}
		return result;
	}

}

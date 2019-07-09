package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.attributes.ResultAttributes;
import com.github.mengxianun.core.interceptor.Entrance;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.json.JsonAttributes;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Executor {

	@Entrance
	public JsonElement execute(String json) {
		Action action = new JsonParser(json).parse();
		JsonElement result = execute(action);
		return result;
	}

	private JsonElement execute(Action action) {

		JsonElement result = null;

		if (action.isStruct()) {
			TableItem tableItem = action.getTableItems().get(0);
			Table table = tableItem.getTable();
			result = new Gson().toJsonTree(table);
		} else if (action.isTransaction()) {
			JsonObject jsonData = action.getRequestData();
			JsonArray transactionArray = jsonData.getAsJsonArray(JsonAttributes.TRANSACTION);
			List<Action> actions = new ArrayList<>();
			for (int i = 0; i < transactionArray.size(); i++) {
				JsonObject innerJsonData = transactionArray.get(i).getAsJsonObject();
				JsonParser innerJsonParser = new JsonParser(innerJsonData);
				Action innerAction = innerJsonParser.parse();
				innerAction.build();
				actions.add(innerAction);
			}
			result = App.currentDataContext().action(actions.toArray(new Action[] {}));
		} else if (action.isNative()) {
			TableItem tableItem = action.getTableItems().get(0);
			Table table = tableItem.getTable();
			result = App.currentDataContext().executeNative(table, action.getNativeContent());
		} else if (action.isTemplate()) {
			// to do
		} else if (action.isResultFile()) {
			// to do
		} else {
			action.build();
			result = App.currentDataContext().action(action);
			result = new DataRenderer().render(result, action);
			if (action.isLimit()) {
				LimitItem limitItem = action.getLimitItem();
				long start = limitItem.getStart();
				long end = limitItem.getEnd();
				JsonElement countElement = App.currentDataContext().action(action.count());
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

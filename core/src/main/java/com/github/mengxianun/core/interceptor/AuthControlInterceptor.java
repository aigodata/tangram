package com.github.mengxianun.core.interceptor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.mengxianun.core.JsonParser;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.attributes.ConfigAttributes;
import com.github.mengxianun.core.exception.PreHandlerException;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.utils.Type;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AuthControlInterceptor implements TranslatorInterceptor {

	private static final String KEY = "auth_control";
	private static final String DEFAULT_STATUS = "off";
	@Override
	public void preHandler(JsonParser parser, JsonObject properties) throws PreHandlerException {

		// 从air.json里取【pre_handler】的【auth_control】的开关
		if (Type.HandlerStatus.ON.status().equalsIgnoreCase(getStatus(properties))) {
			boolean hasAuth = true;

			// TODO 参数里的模块名 "moduleOperate":"moduleName"
			String moduleName = parser.getModuleName();
			// TODO 参数里的模块的有权限的操作："moduleOperate":["add","update","delete","query","import"]
			List<String> moduleOperates = parser.getModuleOperates();

//			String[] moduleTables = {"table1","table2","table3","table4"};
			List<String> moduleTables = getAuthTables(properties, moduleName);

			// 验证是否有当前操作权限
			if (moduleOperates.contains(getOperation(parser))) {
				hasAuth = false;
			}
			// 验证是否有表的操作权限
			for (String operateTable : getOperateTables(parser)) {
				if (!moduleTables.contains(operateTable)) {
					hasAuth = false;
				}
			}
			if (!hasAuth) {
				throw new PreHandlerException(ResultStatus.AUTHENTICATION);
			}
		}
	}
	private List<String> getOperateTables(JsonParser parser) {
		List<String> operateTables = new ArrayList<>();

		// 取主表表名
		for (TableItem tableItem : parser.getAction().getTableItems()) {
			operateTables.add(tableItem.getTable().getName());
		}
		// 取join表表名
		for (JoinItem joinItem : parser.getAction().getJoinItems()) {
			try {
				operateTables.add(joinItem.getRightColumns().get(0).getTableItem().getTable().getName());
			} catch (Exception e) {
			}
		}
		return operateTables;
	}
	private String getOperation(JsonParser parser) {
		String operation = "";
		switch (parser.getOperation()) {
			case DETAIL:
			case QUERY:
			case SELECT:
				operation = "query";
				break;
			case INSERT:
				operation = "add";
				break;
			case UPDATE:
				operation = "update";
				break;
			case DELETE:
				operation = "delete";
				break;
			case NATIVE:
				// TODO
				break;
			default:
				break;
		}
		return operation;
	}
	private String getStatus(JsonObject properties) {
		return getStr(getObj(properties, ConfigAttributes.PRE_HANDLER), KEY, DEFAULT_STATUS);
	}
	private List<String> getAuthTables(JsonObject properties, String moduleName) {
		List<String> list = new ArrayList<>();
		Iterator<JsonElement> iterator = getArr(getObj(properties, KEY), moduleName).iterator();
		while (iterator.hasNext()) {
			JsonElement element = iterator.next();
			list.add(element.getAsString());
		}
		return list;
	}
	private JsonObject getObj(JsonObject json, String key) {
		return json.get(key) != null && json.get(key).isJsonObject() ? (JsonObject) json.get(key) : new JsonObject();
	}
	private JsonArray getArr(JsonObject json, String key) {
		return json.get(key) != null && json.get(key).isJsonArray() ? (JsonArray) json.get(key) : new JsonArray();
	}
	private String getStr(JsonObject json, String key, String defaultValue) {
		return json.get(key) != null && "".equals(json.get(key).toString()) ? json.get(key).toString() : defaultValue;
	}
}

package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc function test")
public class FunctionTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/fun/";

	@Test
	void testDateTime() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "datetime.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject rowObject = result.get(0).getAsJsonObject();
		assertTrue(rowObject.has("ID"));
		assertTrue(rowObject.get("date_year").getAsString().equals("2010.0"));
		assertTrue(rowObject.get("date_month").getAsString().equals("1.0"));
		assertTrue(rowObject.get("date_day").getAsString().equals("1.0"));

		assertTrue(rowObject.get("time_hour").getAsString().equals("10.0"));
		assertTrue(rowObject.get("time_minute").getAsString().equals("10.0"));
		assertTrue(rowObject.get("time_second").getAsString().equals("10.0"));

		assertTrue(rowObject.get("timestamp_year").getAsString().equals("2010.0"));
		assertTrue(rowObject.get("timestamp_month").getAsString().equals("1.0"));
		assertTrue(rowObject.get("timestamp_day").getAsString().equals("1.0"));
		assertTrue(rowObject.get("timestamp_hour").getAsString().equals("10.0"));
		assertTrue(rowObject.get("timestamp_minute").getAsString().equals("10.0"));
		assertTrue(rowObject.get("timestamp_second").getAsString().equals("10.0"));
	}

	@Test
	void testDateTimeGroupOrderLimit() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "datetime_group_order_limit.json");
		JsonObject result = (JsonObject) dataResultSet.getJsonData();
		assertTrue(result.has(ResultAttributes.TOTAL));
		long total = result.get(ResultAttributes.TOTAL).getAsLong();
		assertEquals(4, total);
		JsonArray data = result.get(ResultAttributes.DATA).getAsJsonArray();
		assertEquals(2, data.size());
		JsonObject rowObject1 = data.get(0).getAsJsonObject();
		assertTrue(rowObject1.get("$day(timestamp_col)").getAsString().equals("1.0"));
		assertTrue(rowObject1.get("timestamp_hour").getAsString().equals("12.0"));
		assertTrue(rowObject1.get("count").getAsString().equals("2.0"));
		JsonObject rowObject2 = data.get(1).getAsJsonObject();
		assertTrue(rowObject2.get("$day(timestamp_col)").getAsString().equals("25.0"));
		assertTrue(rowObject2.get("timestamp_hour").getAsString().equals("5.0"));
		assertTrue(rowObject2.get("count").getAsString().equals("1.0"));

	}

}

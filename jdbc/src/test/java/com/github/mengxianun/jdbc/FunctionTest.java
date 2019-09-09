package com.github.mengxianun.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.mengxianun.core.DataResultSet;
import com.github.mengxianun.core.config.ResultAttributes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@DisplayName("Jdbc function test")
public class FunctionTest extends TestSupport {

	private static final String JSON_PARENT_PATH = "json/function/";

	@Test
	void testDateTime() {
		DataResultSet dataResultSet = run(JSON_PARENT_PATH + "datetime.json");
		JsonArray result = (JsonArray) dataResultSet.getJsonData();
		assertTrue(result.size() > 0);
		JsonObject rowObject = result.get(0).getAsJsonObject();

		assertEquals(2010, rowObject.get("date_year").getAsLong());
		assertEquals(1, rowObject.get("date_month").getAsLong());
		assertEquals(1, rowObject.get("date_day").getAsLong());

		assertEquals(10, rowObject.get("time_hour").getAsLong());
		assertEquals(10, rowObject.get("time_minute").getAsLong());
		assertEquals(10, rowObject.get("time_second").getAsLong());

		assertEquals(2010, rowObject.get("timestamp_year").getAsLong());
		assertEquals(1, rowObject.get("timestamp_month").getAsLong());
		assertEquals(1, rowObject.get("timestamp_day").getAsLong());
		assertEquals(10, rowObject.get("timestamp_hour").getAsLong());
		assertEquals(10, rowObject.get("timestamp_minute").getAsLong());
		assertEquals(10, rowObject.get("timestamp_second").getAsLong());
	}

	@Test
	void testNonTime() {
		assertThrows(JdbcDataException.class, () -> run(JSON_PARENT_PATH + "datetime_non_time.json"));
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
		assertEquals(1, rowObject1.get("$day(timestamp_col)").getAsLong());
		assertEquals(12, rowObject1.get("timestamp_hour").getAsLong());
		assertEquals(2, rowObject1.get("count").getAsLong());

		JsonObject rowObject2 = data.get(1).getAsJsonObject();
		assertEquals(25, rowObject2.get("$day(timestamp_col)").getAsLong());
		assertEquals(5, rowObject2.get("timestamp_hour").getAsLong());
		assertEquals(1, rowObject2.get("count").getAsLong());

	}

}

package com.github.mengxianun.core.data.update;

import java.util.Map;

import com.github.mengxianun.core.attributes.ResultAttributes;
import com.google.common.collect.ImmutableMap;

public class InsertSummary implements UpdateSummary {

	private final Object[] generatedKeys;

	public InsertSummary(Object[] generatedKeys) {
		this.generatedKeys = generatedKeys;
	}

	@Override
	public int getUpdateCount() {
		return generatedKeys.length;
	}

	@Override
	public Map<String, Object> getSummary() {
		return ImmutableMap.of(ResultAttributes.PRIMARY_KEY, generatedKeys);
	}

}

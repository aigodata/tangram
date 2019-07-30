package com.github.mengxianun.core.data.update;

import com.github.mengxianun.core.config.ResultAttributes;
import com.google.common.collect.ImmutableMap;

public class DefaultUpdateSummary implements UpdateSummary {

	private final int updateCount;

	public DefaultUpdateSummary(int updateCount) {
		this.updateCount = updateCount;
	}

	@Override
	public int getUpdateCount() {
		return updateCount;
	}

	@Override
	public Object getSummary() {
		return ImmutableMap.of(ResultAttributes.COUNT, updateCount);
	}

}

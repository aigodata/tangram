package com.github.mengxianun.core.data.summary;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.data.AbstractSummary;
import com.google.common.collect.ImmutableMap;

public class UpdateSummary extends AbstractSummary {

	private final int updateCount;

	public UpdateSummary(Action action, int updateCount) {
		super(action, updateCount);
		this.updateCount = updateCount;
	}

	@Override
	public Object getData() {
		return ImmutableMap.of(ResultAttributes.COUNT, updateCount);
	}

}

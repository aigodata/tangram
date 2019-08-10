package com.github.mengxianun.core.data.summary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.AbstractSummary;

public class InsertSummary extends AbstractSummary {

	private final List<Map<String, Object>> values;

	public InsertSummary(Action action, List<Map<String, Object>> values) {
		super(action, values);
		this.values = values;
	}

	@Override
	public Object getData() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}
		if (values.size() == 1) {
			return values.get(0);
		}
		return values;
	}

}

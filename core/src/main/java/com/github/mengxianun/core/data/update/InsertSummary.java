package com.github.mengxianun.core.data.update;

import java.util.List;
import java.util.Map;

public class InsertSummary implements UpdateSummary {

	// 插入的数据内容
	private final List<Map<String, Object>> contents;

	public InsertSummary(List<Map<String, Object>> contents) {
		this.contents = contents;
	}

	@Override
	public int getUpdateCount() {
		return contents.size();
	}

	@Override
	public Object getSummary() {
		if (contents.size() == 1) {
			return contents.get(0);
		}
		return contents;
	}

	public List<Map<String, Object>> getContents() {
		return contents;
	}

}

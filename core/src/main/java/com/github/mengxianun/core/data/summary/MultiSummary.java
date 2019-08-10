package com.github.mengxianun.core.data.summary;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.mengxianun.core.data.AbstractSummary;
import com.github.mengxianun.core.data.Summary;

public class MultiSummary extends AbstractSummary {

	private final List<Summary> summaries;

	public MultiSummary() {
		super(null, null);
		this.summaries = new ArrayList<>();
	}

	public MultiSummary(List<Summary> summaries) {
		super(null, summaries);
		this.summaries = summaries;
	}

	public void add(Summary summary) {
		this.summaries.add(summary);
	}

	public List<Summary> getSummaries() {
		return summaries;
	}

	@Override
	public Object getData() {
		return summaries.stream().map(Summary::getData).collect(Collectors.toList());
	}

}

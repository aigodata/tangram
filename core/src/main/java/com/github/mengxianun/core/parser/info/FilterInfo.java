package com.github.mengxianun.core.parser.info;

import java.util.List;

import javax.annotation.Nullable;

import com.github.mengxianun.core.request.Connector;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class FilterInfo {

	public static FilterInfo create(Connector connector, @Nullable ConditionInfo conditionInfo,
			List<FilterInfo> subfilters) {
		return new AutoValue_FilterInfo(connector, conditionInfo, subfilters);
	}

	public abstract Connector connector();

	@Nullable
	public abstract ConditionInfo conditionInfo();

	public abstract List<FilterInfo> subfilters();

}

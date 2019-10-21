package com.github.mengxianun.core.permission;

import com.github.mengxianun.core.request.Connector;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ConnectorCondition {

	public static ConnectorCondition create(Condition condition) {
		return new AutoValue_ConnectorCondition(Connector.AND, condition);
	}

	public static ConnectorCondition create(Connector connector, Condition condition) {
		return new AutoValue_ConnectorCondition(connector, condition);
	}

	public abstract Connector connector();

	public abstract Condition condition();

}

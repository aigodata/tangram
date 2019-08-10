package com.github.mengxianun.core.data;

import com.github.mengxianun.core.Action;

public abstract class AbstractSummary implements Summary {

	protected final Action action;
	protected final Object data;

	public AbstractSummary(Action action, Object data) {
		this.action = action;
		this.data = data;
	}

	@Override
	public Action getAction() {
		return action;
	}

	@Override
	public Object getData() {
		return data;
	}

}

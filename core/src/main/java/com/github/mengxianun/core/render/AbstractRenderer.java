package com.github.mengxianun.core.render;

import com.github.mengxianun.core.Action;

public abstract class AbstractRenderer<T> implements Renderer<T> {

	protected final Action action;

	public AbstractRenderer(Action action) {
		this.action = action;
	}

}

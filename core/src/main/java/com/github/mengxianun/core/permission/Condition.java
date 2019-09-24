package com.github.mengxianun.core.permission;

import com.github.mengxianun.core.Action;

public interface Condition {

	public void process(Action action);

}

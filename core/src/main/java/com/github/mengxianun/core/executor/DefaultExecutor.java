package com.github.mengxianun.core.executor;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.resutset.DataResult;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class DefaultExecutor extends AbstractExecutor {

	@Inject
	public DefaultExecutor(@Assisted DataContext dataContext) {
		super(dataContext);
	}

	public DataResult execute(Action action) {
		return dataContext.execute(action);
	}

}

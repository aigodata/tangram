package com.github.mengxianun.core.executor;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.JsonParser;
import com.github.mengxianun.core.resutset.DataResult;

public abstract class AbstractExecutor implements Executor {

	protected final DataContext dataContext;

	public AbstractExecutor(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	@Override
	public DataResult execute(String json) {
		Action action = new JsonParser(json).parse();
		return execute(action);
	}

	protected abstract DataResult execute(Action action);

}

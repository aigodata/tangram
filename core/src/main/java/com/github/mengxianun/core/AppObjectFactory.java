package com.github.mengxianun.core;

import com.github.mengxianun.core.executor.Executor;

public interface AppObjectFactory {

	public Executor createExecutor(DataContext dataContext);

}

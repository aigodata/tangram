package com.github.mengxianun.core;

import com.github.mengxianun.core.executor.DefaultExecutor;
import com.github.mengxianun.core.executor.Executor;
import com.github.mengxianun.core.interceptor.entrance.Entrance;
import com.github.mengxianun.core.interceptor.entrance.EntranceInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.matcher.Matchers;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		// 入口拦截器
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Entrance.class), new EntranceInterceptor());
		// 对象工厂
		install(new FactoryModuleBuilder().implement(Executor.class, DefaultExecutor.class)
				.build(AppObjectFactory.class));
	}

}

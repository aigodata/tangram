package com.github.mengxianun.core;

import com.github.mengxianun.core.interceptor.Entrance;
import com.github.mengxianun.core.interceptor.EntranceInterceptor;
import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		// 入口拦截器
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(Entrance.class), new EntranceInterceptor());
	}

}

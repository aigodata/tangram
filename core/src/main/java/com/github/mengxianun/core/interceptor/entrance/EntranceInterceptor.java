package com.github.mengxianun.core.interceptor.entrance;

import java.time.Duration;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.JsonParser;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;

public class EntranceInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(EntranceInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// 设置当前线程的上下文
		String requestJson = invocation.getArguments()[0].toString();
		String sourceName = new JsonParser(requestJson).parseSource();
		if (Strings.isNullOrEmpty(sourceName)) {
			sourceName = App.getDefaultDataSource();
		}
		App.setCurrentDataContext(sourceName);
		if (logger.isDebugEnabled()) {
			logger.debug("Request: {}", requestJson);
		}
		// Stopwatch
		Stopwatch stopwatch = Stopwatch.createStarted();
		// Run
		Object result = invocation.proceed();
		// Done
		Duration duration = stopwatch.stop().elapsed();
		if (logger.isDebugEnabled()) {
			logger.debug("Operation is completed, taking {} milliseconds", duration.toMillis());
		}
		// 清理当前线程的上下文
		App.cleanup();
		return result;
	}

}

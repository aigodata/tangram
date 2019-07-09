package com.github.mengxianun.core.interceptor;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.App;
import com.github.mengxianun.core.JsonParser;
import com.google.common.base.Strings;

public class EntranceInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(EntranceInterceptor.class);

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug("Entry EntranceInterceptor");
		}
		// 设置当前线程的上下文
		String requestJson = invocation.getArguments()[0].toString();
		String sourceName = new JsonParser(requestJson).parseSource();
		if (Strings.isNullOrEmpty(sourceName)) {
			sourceName = App.getDefaultDataSource();
		}
		App.setCurrentDataContext(sourceName);

		Object result = invocation.proceed();
		// 清理当前线程的上下文
		App.cleanup();
		return result;
	}

}

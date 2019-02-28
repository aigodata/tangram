package com.github.mengxianun.encrypt;

import com.github.mengxianun.core.timer.TimerFactory;
import com.github.mengxianun.core.utils.Type;
import com.google.auto.service.AutoService;

@AutoService(TimerFactory.class)
public final class RefreshSecretKeyTimerFactory implements TimerFactory {

	@Override
	public String getType() {
		return Type.TimerType.ENCRYPT.type();
	}

	@Override
	public RefreshSecretKeyTimer create() {
		return new RefreshSecretKeyTimer();
	}

}

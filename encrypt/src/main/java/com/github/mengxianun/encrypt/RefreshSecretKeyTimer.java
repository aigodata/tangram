package com.github.mengxianun.encrypt;

import com.github.mengxianun.core.timer.Timer;

public class RefreshSecretKeyTimer implements Timer {

	public RefreshSecretKeyTimer() {

	}
	@Override
	public void start(Integer aliveTimeMinute, Integer destroyTimeMinute) {

		RefreshSecretKeyTask task = RefreshSecretKeyTask.getInstance(aliveTimeMinute, destroyTimeMinute);
		task.start();
	}
	@Override
	public void stop() {

		RefreshSecretKeyTask task = RefreshSecretKeyTask.getInstance();
		task.stop();
	}
}

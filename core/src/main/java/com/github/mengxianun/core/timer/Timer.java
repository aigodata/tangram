package com.github.mengxianun.core.timer;

public interface Timer {

	void start(Integer aliveTimeMinute, Integer destroyTimeMinute);
	void stop();
}

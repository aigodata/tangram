package com.github.mengxianun.core.timer;

public interface TimerFactory {

	String getType();

	Timer create();
}

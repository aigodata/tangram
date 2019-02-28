package com.github.mengxianun.encrypt;

import java.io.Serializable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

public class RefreshSecretKeyTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private volatile static RefreshSecretKeyTask task;

	private RefreshSecretKeyTask() {

	}
	public static RefreshSecretKeyTask getInstance() {
		// 双重检查加锁
		if (task == null) {
			synchronized (RefreshSecretKeyTask.class) {
				if (task == null) {
					task = new RefreshSecretKeyTask();
				}
			}
		}
		return task;
	}
	public static RefreshSecretKeyTask getInstance(Integer aliveTimeMinute, Integer destroyTimeMinute) {
		// 双重检查加锁
		if (task == null) {
			synchronized (RefreshSecretKeyTask.class) {
				if (task == null) {
					task = new RefreshSecretKeyTask();
				}
			}
		}
		task.setAliveTime(aliveTimeMinute);
		task.setDestroyTime(destroyTimeMinute);
		return task;
	}
	public void setAliveTime(Integer aliveTimeMinute) {
		if (aliveTimeMinute != null && aliveTimeMinute > 0) {
			this.periodTime = aliveTimeMinute;
		}
	}
	public void setDestroyTime(Integer destroyTimeMinute) {
		if (destroyTimeMinute != null && destroyTimeMinute > 0) {
			this.delayTime = destroyTimeMinute;
		}
	}

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private Future createFuture = null;
	private Future destroyFuture = null;


	private int periodTime = 5;

	private int delayTime = 6;

	public void start() {
		// 重复调用的时候，先要关闭（这个得测测）
		stop();

		final Runnable createHandler = new Runnable() {

			@Override
			public void run() {
				SecretKeySingleEntity entity = SecretKeySingleEntity.getInstance();
				entity.createSecretKey(delayTime, periodTime);
			}
		};
		// 每5分钟(默认)执行一次，立即执行
		createFuture = scheduler.scheduleAtFixedRate(createHandler, 0,  periodTime * 60, SECONDS);

		final Runnable destroyHandler = new Runnable() {

			@Override
			public void run() {
				SecretKeySingleEntity entity = SecretKeySingleEntity.getInstance();
				entity.destroyOldestSecretKey();
			}
		};
		// 每5分钟(默认)执行一次，延迟6分钟(默认)执行
		destroyFuture = scheduler.scheduleAtFixedRate(destroyHandler, this.delayTime * 60, periodTime * 60, SECONDS);

	}
	public void stop() {
		if (createFuture != null && !createFuture.isCancelled()) {
			createFuture.cancel(true);
			createFuture = null;
		}
		if (destroyFuture != null && !destroyFuture.isCancelled()) {
			destroyFuture.cancel(true);
			destroyFuture = null;
		}
	}
}

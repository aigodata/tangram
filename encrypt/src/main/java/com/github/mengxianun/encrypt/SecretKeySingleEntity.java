package com.github.mengxianun.encrypt;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SecretKeySingleEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private volatile static SecretKeySingleEntity entity;

	private SecretKeySingleEntity() {

	}
	public static SecretKeySingleEntity getInstance() {
		// 双重检查加锁
		if (entity == null) {
			synchronized (SecretKeySingleEntity.class) {
				if (entity == null) {
					entity = new SecretKeySingleEntity();
				}
			}
		}
		return entity;
	}
	private static ConcurrentHashMap<Long, Map<String, Object>> secretKeyMap = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Long, Long> nextTimeMap = new ConcurrentHashMap<>();

	private static Long bufferTime = 0L;
	/**
	 * 生成新的秘钥
	 */
	protected void createSecretKey(int destroyTimeMinute, int createTimeMinute) {
		synchronized (entity) {
			String[] keyAndIV = AES.getKeyAndIV();
			String key = keyAndIV[0];
			String iv = keyAndIV[1];
			String uid = AES.getUUID();

			Map<String, Object> map = new HashMap<>();
			map.put(AES.KEY, key);
			map.put(AES.IV, iv);
			map.put(AES.UID, uid);
			secretKeyMap.put(deathTimeL(destroyTimeMinute), map);

			// TODO Test
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
			System.out.println("===============刚刚生成了一条密钥================="+format.format(Calendar.getInstance().getTimeInMillis()));
			for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
				System.out.println("预计死亡时间 : " + format.format(entry.getKey()));
				System.out.println("KEY        : " + entry.getValue().get(AES.KEY));
				System.out.println("IV         : " + entry.getValue().get(AES.IV));
				System.out.println("UID        : " + entry.getValue().get(AES.UID));
				System.out.println("---------------------------------------------");
			}
			System.out.println("================================================");
			bufferTime = (destroyTimeMinute - createTimeMinute)*60*1000L;
		}
	}
	private long deathTimeL(int destroyTimeSeconds) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, destroyTimeSeconds);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
	}
	/**
	 * 摧毁最旧的秘钥
	 */
	protected void destroyOldestSecretKey() {
		synchronized (entity) {
			Calendar now = Calendar.getInstance();
			for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
				// 死亡时间 小于 当前时间，当前密钥 game over
				if (entry.getKey() <= now.getTimeInMillis()) {
					secretKeyMap.remove(entry.getKey());
				}
			}
			// TODO Test
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			System.out.println("===============刚刚执行了销毁密钥================="+format.format(Calendar.getInstance().getTimeInMillis()));
			for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
				System.out.println("预计死亡时间: " + format.format(entry.getKey()));
				System.out.println("KEY       :" + entry.getValue().get(AES.KEY));
				System.out.println("IV        :" + entry.getValue().get(AES.IV));
				System.out.println("UID       :" + entry.getValue().get(AES.UID));
				System.out.println("-------------------------------------------");
			}
			System.out.println("================================================");
		}

	}
	public String getKey(String uid) {
		return getKey(uid, AES.KEY);
	}
	public String getIV(String uid) {
		return getKey(uid, AES.IV);
	}
	public String getKey(String uid, String objectKey) {
		for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
			if (entry.getValue().get(AES.UID).equals(uid)) {
				return entry.getValue().get(objectKey).toString();
			}
		}
		return null;
	}
	protected Map<Long, Map<String, Object>> getAllInfo() {
		return secretKeyMap;
	}
	public List<String> getAliveKeys() {
		return getObjectArray(AES.KEY);
	}
	public List<String> getAliveUids() {
		return getObjectArray(AES.UID);
	}
	public List<String> getAliveIVs() {
		return getObjectArray(AES.IV);
	}
	public List<String> getObjectArray(String objectKey) {
		List<String> list = new ArrayList<>();
		for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
			list.add(entry.getValue().get(objectKey).toString());
		}
		return list;
	}
	public Map<String, Object> getSecretKeyAndIv(String uid) {
		for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
			if (entry.getValue().get(AES.UID).equals(uid)) {
				return entry.getValue();
			}
		}
		return null;
	}
	public Map<String, Object> getLatestInfo() {
		Map<String, Object> map = new HashMap<>();
		Long lastestTime = 0L;
		for (Map.Entry<Long, Map<String, Object>> entry : secretKeyMap.entrySet()) {
			if (entry.getKey() > lastestTime) {
				lastestTime = entry.getKey();
			}
		}
		// 不用contains的原因是，Long型在contains比较的时候会失败。。。巨坑
		if (secretKeyMap.get(lastestTime) != null) {
			map.putAll(secretKeyMap.get(lastestTime));
			map.put("deathTime", lastestTime-Calendar.getInstance().getTimeInMillis()-bufferTime+2000);//延迟两秒
		}
		return map;
	}

}

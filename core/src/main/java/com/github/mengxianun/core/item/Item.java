package com.github.mengxianun.core.item;

import java.io.Serializable;
import java.util.Random;

public abstract class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String getRandomAlias() {
		return getRandomString(6);
	}

	protected String getRandomString(int length) {
		// String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		String base = "abcdefghijklmnopqrstuvwxyz";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

}

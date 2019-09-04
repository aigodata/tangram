package com.github.mengxianun.core;

import java.time.LocalDateTime;

public enum Keywords implements Keyword {

	NOW {

		@Override
		public Object parse() {
			return LocalDateTime.now();
		}

	},
	UUID {

		@Override
		public Object parse() {
			return java.util.UUID.randomUUID().toString().replaceAll("-", "");
		}

	}

}

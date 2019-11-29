package com.github.mengxianun.core;

import java.sql.Timestamp;
import java.util.Date;

public enum Keywords implements Keyword {

	NOW {

		@Override
		public Object parse() {
			// optimize
			return new Timestamp(new Date().getTime());
			//			return LocalDateTime.now();
		}

	},
	UUID {

		@Override
		public Object parse() {
			return java.util.UUID.randomUUID().toString().replaceAll("-", "");
		}

	}

}

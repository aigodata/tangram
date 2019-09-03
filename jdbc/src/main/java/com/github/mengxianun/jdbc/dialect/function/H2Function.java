package com.github.mengxianun.jdbc.dialect.function;

import com.github.mengxianun.core.dialect.Function;

public enum H2Function implements Function {

	YEAR {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	},
	MONTH {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	},
	DAY {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	},
	HOUR {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	},
	MINUTE {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	},
	SECOND {

		@Override
		public String convert(String func, String args) {
			return toTimeFunc(func, args);
		}

	};
	
	public String toTimeFunc(String func, String args) {
		return func + "(" + args + ")";
	}

}

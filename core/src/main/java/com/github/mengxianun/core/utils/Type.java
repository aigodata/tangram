package com.github.mengxianun.core.utils;

public class Type {

	public enum ConverterType {

		EXCEL("excel"),
		PDF("pdf"),
		WORD("word"),
		HTML("html");

		private String type;

		ConverterType(String type) {
			this.type = type;
		}
		public String type() {
			return type;
		}
	}
	public enum ConverterSuffix {

		EXCEL(".xls"),
		PDF(".pdf"),
		WORD(".doc"),
		HTML(".html");

		private String suffix;

		ConverterSuffix(String suffix) {
			this.suffix = suffix;
		}
		public String suffix() {
			return suffix;
		}
	}
	public enum TimerType {

		ENCRYPT("encrypt");

		private String type;

		TimerType(String type) {
			this.type = type;
		}
		public String type() {
			return type;
		}
	}

	public enum HandlerStatus {

		ON("on"),
		OFF("off");

		private String status;

		HandlerStatus(String status) {
			this.status = status;
		}
		public String status() {
			return status;
		}
	}
}

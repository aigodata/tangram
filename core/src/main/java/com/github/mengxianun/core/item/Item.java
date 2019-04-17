package com.github.mengxianun.core.item;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;

public abstract class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	public Object getRealValue(Column column, Object value) {
		if (value == null) {
			return null;
		}
		if (column != null) {
			ColumnType columnType = column.getType();
			if (columnType.isLiteral()) {
				return String.valueOf(value);
			} else if (columnType.isNumber()) {
				try {
					return NumberFormat.getInstance().parse(value.toString());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
				}
			} else if (columnType.isTimeBased()) {
				if (columnType.isDate()) {
					return LocalDate.parse(value.toString());
				} else if (columnType.isTime()) {
					return LocalTime.parse(value.toString());
				} else if (columnType.isTimestamp()) {
					return Timestamp.valueOf(value.toString());
				}
			} else if (columnType.isBoolean()) {
				return Boolean.parseBoolean(value.toString());
			}
		}
		return value;
	}

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

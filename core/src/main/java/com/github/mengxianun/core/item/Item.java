package com.github.mengxianun.core.item;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;

import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;

public abstract class Item implements Serializable {

	private static final long serialVersionUID = 1L;

	public Object getRealValue(Column column, Object value) {
		if (value == null) {
			return null;
		}
		if (value.getClass().isArray()) {
			return getRealValueInArray(column, value);
		}
		if (column != null) {
			ColumnType columnType = column.getType();
			if (columnType.isLiteral()) {
				return String.valueOf(value);
			} else if (columnType.isNumber()) {
				try {
					return NumberFormat.getInstance().parse(value.toString());
				} catch (ParseException e) {
					// ignore
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

	public Object getRealValueInArray(Column column, Object value) {
		if (value == null) {
			return null;
		}
		if (column != null) {
			// 原始值
			Object[] ogValueArray = (Object[]) value;
			// 实际值
			Object[] realValueArray = new Object[ogValueArray.length];
			for (int i = 0; i < ogValueArray.length; i++) {
				realValueArray[i] = getRealValue(column, ogValueArray[i]);
			}
			return realValueArray;
		}
		return value;
	}

}

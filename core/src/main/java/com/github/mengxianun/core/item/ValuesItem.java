package com.github.mengxianun.core.item;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.github.mengxianun.core.Keyword;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;

/**
 * Value operation
 * 
 * @author mengxiangyun
 *
 */
public class ValuesItem extends Item {

	private static final long serialVersionUID = 1L;
	protected Object value;

	public ValuesItem(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getRealValue(Column column, Object value) {
		if (value == null) {
			return null;
		}
		if (value.getClass().isArray()) {
			return getRealValueInArray(column, value);
		}
		if (Keyword.DATE_NOW.equals(value)) {
			return LocalDateTime.now();
		}
		if (column != null) {
			value = getRealValue(column.getType(), value);
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

	private Object getRealValue(ColumnType columnType, Object value) {
		if (columnType.isLiteral()) {
			return String.valueOf(value);
		} else if (columnType.isNumber()) {
			try {
				return NumberFormat.getInstance().parse(value.toString());
			} catch (ParseException ignore) {}
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
		return value;
	}

}

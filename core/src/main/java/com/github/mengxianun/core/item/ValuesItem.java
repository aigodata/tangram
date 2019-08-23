package com.github.mengxianun.core.item;

import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.github.mengxianun.core.Keyword;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;
import com.joestelmach.natty.Parser;

/**
 * Value operation
 * 
 * @author mengxiangyun
 *
 */
public class ValuesItem extends Item {

	private static final long serialVersionUID = 1L;

	private Parser parser = new Parser();
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
		} else if (columnType.isNumber() || columnType.isNumberArray()) {
			try {
				Number number = NumberFormat.getInstance().parse(value.toString());

				// ! Gson 将所有数值类型转成Double类型, 这里将值转换为真实类型
				if (columnType.isInteger() || columnType.isIntArray()) {
					number = number.intValue();
				}
				return number;
			} catch (ParseException ignore) {}
		} else if (columnType.isTimeBased()) {
			Date date = parser.parse(value.toString()).get(0).getDates().get(0);
			return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		} else if (columnType.isBoolean()) {
			return Boolean.parseBoolean(value.toString());
		}
		return value;
	}

}

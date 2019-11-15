package com.github.mengxianun.core.render;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.schema.Column;
import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public abstract class AbstractRenderer<T> implements Renderer<T> {

	protected static final Parser parser = new Parser();

	protected final Action action;

	public AbstractRenderer(Action action) {
		this.action = action;
	}

	protected String getColumnKey(ColumnItem columnItem) {
		String columnKey = "";
		Column column = columnItem.getColumn();
		if (columnItem.isCustomAlias()) { // 自定义别名
			columnKey = columnItem.getAlias();
		} else if (column == null) { // 表达式, 如函数
			columnKey = columnItem.getExpression();
		} else {
			columnKey = column.getAliasOrName();
		}
		return columnKey;
	}

	protected String parseTimeValue(Object value, String timeFormat) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(timeFormat);
		String timeString = value.toString();
		try {
			timeString = parseTimeValue(timeString, formatter);
		} catch (Exception e) {
			timeString = parseUnrecognizedTimeValue(value, timeFormat);
		}
		return timeString;

	}

	private String parseTimeValue(String timeString, DateTimeFormatter formatter) {
		if (NumberUtils.isDigits(timeString)) {
			Long millis = Long.valueOf(timeString);
			LocalDateTime localDateTime = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime();
			timeString = localDateTime.format(formatter);
		} else {
			timeString = LocalDateTime.parse(timeString).format(formatter);
		}
		return timeString;
	}

	private String parseUnrecognizedTimeValue(Object value, String timeFormat) {
		List<DateGroup> groups = parser.parse(value.toString());
		if (groups.isEmpty()) {
			throw new DateTimeException(String.format("Unable to parse date/time [%s]", value));
		}
		Date date = groups.get(0).getDates().get(0);
		LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return localDateTime.format(DateTimeFormatter.ofPattern(timeFormat));
	}

}

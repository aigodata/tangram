package com.github.mengxianun.core.item.extension;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.request.Connector;
import com.github.mengxianun.core.request.Operator;

public class StatementValueFilterItem extends FilterItem {

	private static final long serialVersionUID = 1L;

	public StatementValueFilterItem(Connector connector, ColumnItem columnItem, Operator operator, Object statement) {
		super(connector, columnItem, operator, statement);
	}

	@Override
	public Object getRealValue() {
		return value;
	}

}

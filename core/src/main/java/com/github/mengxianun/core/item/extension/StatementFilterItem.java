package com.github.mengxianun.core.item.extension;

import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.request.Connector;

public class StatementFilterItem extends FilterItem {

	private static final long serialVersionUID = 1L;

	public StatementFilterItem(Connector connector, String statement) {
		super(connector, null, null, statement);
	}

	@Override
	public Object getRealValue() {
		return value;
	}

}

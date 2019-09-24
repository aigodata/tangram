package com.github.mengxianun.core.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.BasicSummary;
import com.github.mengxianun.core.schema.Table;

public class StructAction extends AbstractAction {

	private Table table;

	public StructAction(DataContext dataContext, Table table) {
		super(dataContext);
		this.table = table;
	}

	@Override
	public Summary execute() {
		return new BasicSummary(table.getInfo());
	}

}

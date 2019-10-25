package com.github.mengxianun.core.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.BasicSummary;

public class StructsAction extends AbstractAction {

	public StructsAction(DataContext dataContext) {
		super(dataContext);
	}

	@Override
	public Summary execute() {
		return new BasicSummary(dataContext.getSchema().getInfo());
	}

}

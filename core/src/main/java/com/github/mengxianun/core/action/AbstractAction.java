package com.github.mengxianun.core.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;

public abstract class AbstractAction implements NewAction {

	protected final DataContext dataContext;

	public AbstractAction(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	@Override
	public DataContext getDataContext() {
		return dataContext;
	}

}

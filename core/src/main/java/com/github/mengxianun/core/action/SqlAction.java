package com.github.mengxianun.core.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.data.Summary;

public class SqlAction extends AbstractAction {

	private final String sql;

	public SqlAction(DataContext dataContext, String sql) {
		super(dataContext);
		this.sql = sql;
	}

	@Override
	public Summary execute() {
		return dataContext.executeSql(sql);
	}

}

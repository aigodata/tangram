package com.github.mengxianun.core.parser.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.action.SqlAction;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.SqlInfo;

public class SqlActionParser extends AbstractActionParser {

	public SqlActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
	}

	@Override
	public NewAction parse() {
		SqlInfo sqlInfo = simpleInfo.sql();
		return new SqlAction(dataContext, sqlInfo.sql());
	}

}

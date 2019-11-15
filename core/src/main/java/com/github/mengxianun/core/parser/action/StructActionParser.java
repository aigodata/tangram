package com.github.mengxianun.core.parser.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.ResultStatus;
import com.github.mengxianun.core.action.StructAction;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.parser.info.TableInfo;
import com.github.mengxianun.core.schema.Table;

public class StructActionParser extends AbstractActionParser {

	public StructActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
	}

	@Override
	public NewAction parse() {
		TableInfo tableInfo = simpleInfo.table();
		Table table = dataContext.getTable(tableInfo.table());
		if (table == null) {
			throw new DataException(ResultStatus.DATASOURCE_TABLE_NOT_EXIST, tableInfo.table());
		}
		return new StructAction(dataContext, table);
	}

}

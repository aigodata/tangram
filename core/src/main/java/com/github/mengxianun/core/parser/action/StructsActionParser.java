package com.github.mengxianun.core.parser.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.action.StructsAction;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.info.SimpleInfo;

public class StructsActionParser extends AbstractActionParser {

	public StructsActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
	}

	@Override
	public NewAction parse() {
		return new StructsAction(dataContext);
	}

}

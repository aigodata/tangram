package com.github.mengxianun.core.parser.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.action.NativeAction;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.info.NativeInfo;
import com.github.mengxianun.core.parser.info.SimpleInfo;

public class NativeActionParser extends AbstractActionParser {

	public NativeActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
	}

	@Override
	public NewAction parse() {
		NativeInfo nativeInfo = simpleInfo.nativeInfo();
		return new NativeAction(dataContext, nativeInfo.content());
	}

}

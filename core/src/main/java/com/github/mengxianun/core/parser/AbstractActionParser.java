package com.github.mengxianun.core.parser;

import com.github.mengxianun.core.ActionUtil;
import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.parser.info.SimpleInfo;
import com.github.mengxianun.core.schema.Name;

public abstract class AbstractActionParser implements ActionParser {

	protected final SimpleInfo simpleInfo;
	protected final DataContext dataContext;

	public AbstractActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		this.simpleInfo = simpleInfo;
		this.dataContext = dataContext;
	}

	protected <T extends Name> String getAlias(T element) {
		if (dataContext.getDialect().randomAliasEnabled()) {
			String prefix = element == null ? "" : element.getName() + "_";
			return prefix + ActionUtil.createRandomAlias();
		}
		return null;
	}

}

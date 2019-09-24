package com.github.mengxianun.core.parser.action;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.action.TransactionAction;
import com.github.mengxianun.core.parser.AbstractActionParser;
import com.github.mengxianun.core.parser.ActionParser;
import com.github.mengxianun.core.parser.ParserFactory;
import com.github.mengxianun.core.parser.info.SimpleInfo;

public class TransactionActionParser extends AbstractActionParser {

	public TransactionActionParser(SimpleInfo simpleInfo, DataContext dataContext) {
		super(simpleInfo, dataContext);
	}

	@Override
	public NewAction parse() {
		List<NewAction> actions = new ArrayList<>();
		List<SimpleInfo> simples = simpleInfo.simples();
		for (SimpleInfo simpleInfo : simples) {
			ActionParser actionParser = ParserFactory.getActionParser(simpleInfo, dataContext);
			NewAction action = actionParser.parse();
			actions.add(action);
		}
		return new TransactionAction(dataContext, actions);
	}

}

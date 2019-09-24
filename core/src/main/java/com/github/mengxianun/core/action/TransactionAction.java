package com.github.mengxianun.core.action;

import java.util.List;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.NewAction;
import com.github.mengxianun.core.data.Summary;

public class TransactionAction extends AbstractAction {

	private final List<NewAction> actions;

	public TransactionAction(DataContext dataContext, List<NewAction> actions) {
		super(dataContext);
		this.actions = actions;
	}

	@Override
	public Summary execute() {
		NewAction[] actionArray = new NewAction[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			actionArray[i] = actions.get(i);
		}
		return dataContext.execute(actionArray);
	}

}

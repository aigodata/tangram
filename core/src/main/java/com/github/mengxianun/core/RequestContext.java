package com.github.mengxianun.core;


public class RequestContext {

	private DataContext dataContext;
	private Action action;

	public RequestContext() {}

	public RequestContext(DataContext dataContext, Action action) {
		this.dataContext = dataContext;
		this.action = action;
	}

	public DataContext getDataContext() {
		return dataContext;
	}

	public void setDataContext(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

}

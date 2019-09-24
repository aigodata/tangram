package com.github.mengxianun.core.action;

import com.github.mengxianun.core.DataContext;
import com.github.mengxianun.core.data.Summary;

public class NativeAction extends AbstractAction {

	private final String nativeContent;

	public NativeAction(DataContext dataContext, String nativeContent) {
		super(dataContext);
		this.nativeContent = nativeContent;
	}

	@Override
	public Summary execute() {
		return dataContext.executeNative(nativeContent);
	}

}

package com.github.mengxianun.core;

import com.github.mengxianun.core.data.Summary;

public interface NewAction {

	public DataContext getDataContext();

	public Summary execute();

}

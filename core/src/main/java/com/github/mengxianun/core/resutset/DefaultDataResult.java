package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.data.DataSet;
import com.github.mengxianun.core.data.update.UpdateSummary;

public class DefaultDataResult implements DataResult {

	private final DataSet dataSet;
	private final UpdateSummary updateSummary;
	private final Object data;

	public DefaultDataResult(DataSet dataSet) {
		this.dataSet = dataSet;
		this.updateSummary = null;
		this.data = null;
	}

	public DefaultDataResult(UpdateSummary updateSummary) {
		this.dataSet = null;
		this.updateSummary = updateSummary;
		this.data = null;
	}

	public DefaultDataResult(Object data) {
		this.dataSet = null;
		this.updateSummary = null;
		this.data = data;
	}

	@Override
	public DataSet getDataSet() {
		return dataSet;
	}

	@Override
	public UpdateSummary getUpdateSummary() {
		return updateSummary;
	}

	@Override
	public boolean isQuery() {
		return dataSet != null;
	}

	@Override
	public boolean isUpdate() {
		return updateSummary != null;
	}

	@Override
	public Object getData() {
		return data;
	}

}

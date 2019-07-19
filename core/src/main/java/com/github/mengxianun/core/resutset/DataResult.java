package com.github.mengxianun.core.resutset;

import com.github.mengxianun.core.data.DataSet;
import com.github.mengxianun.core.data.update.UpdateSummary;

public interface DataResult {

	public DataSet getDataSet();

	public UpdateSummary getUpdateSummary();

	public boolean isQuery();

	public boolean isUpdate();

	public Object getData();

}

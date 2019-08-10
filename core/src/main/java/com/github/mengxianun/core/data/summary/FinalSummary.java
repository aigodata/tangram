package com.github.mengxianun.core.data.summary;

import java.util.List;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.data.Row;

/**
 * 不再进行后续处理的结果
 * 
 * @author mengxiangyun
 *
 */
public class FinalSummary extends QuerySummary {

	public FinalSummary(Action action, Object data) {
		super(action, data);
	}

	@Override
	public List<Row> toRows() {
		throw new UnsupportedOperationException();
	}

}

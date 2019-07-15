package com.github.mengxianun.core.render;

import java.util.List;

import com.github.mengxianun.core.data.Row;

public interface Renderer<T> {

	public T render(List<Row> rows);

}

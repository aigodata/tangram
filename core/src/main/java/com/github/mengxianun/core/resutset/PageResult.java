package com.github.mengxianun.core.resutset;

/**
 * 分页结果封装
 * 
 * @author mengxiangyun
 *
 */
public class PageResult {

	private long start;
	private long end;
	private long total;
	private Object data;

	public PageResult() {}

	public PageResult(long start, long end, long total, Object data) {
		this.start = start;
		this.end = end;
		this.total = total;
		this.data = data;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}

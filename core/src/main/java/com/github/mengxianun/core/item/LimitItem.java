package com.github.mengxianun.core.item;

public class LimitItem extends Item {

	private static final long serialVersionUID = 1L;
	// 偏移量, 默认0.
	private long offset = 0;
	// 每页大小, 默认10
	private long limit = 10;
	// 起始位置, 同 offset
	private long start = offset;
	// 结束位置
	private long end;

	public LimitItem(long start, long end) {
		this.start = this.offset = start < 0 ? 0 : start;
		this.end = end < start ? start : end;
		this.limit = end - start;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
		this.start = offset;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
		this.offset = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

}

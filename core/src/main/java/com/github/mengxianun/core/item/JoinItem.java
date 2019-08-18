package com.github.mengxianun.core.item;

import com.github.mengxianun.core.request.JoinType;

public class JoinItem extends Item {

	private static final long serialVersionUID = 1L;
	private final ColumnItem leftColumn;
	private final ColumnItem rightColumn;
	private final JoinType joinType;

	public JoinItem(ColumnItem leftColumn, ColumnItem rightColumn) {
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
		this.joinType = JoinType.LEFT;
	}

	public JoinItem(ColumnItem leftColumn, ColumnItem rightColumn, JoinType joinType) {
		this.leftColumn = leftColumn;
		this.rightColumn = rightColumn;
		this.joinType = joinType;
	}

	public ColumnItem getLeftColumn() {
		return leftColumn;
	}

	public ColumnItem getRightColumn() {
		return rightColumn;
	}

	public JoinType getJoinType() {
		return joinType;
	}

}

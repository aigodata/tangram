package com.github.mengxianun.core.item;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.request.JoinType;

public class JoinItem extends Item {

	private static final long serialVersionUID = 1L;
	private final List<SingleColumnJoinItem> joinItems;
	private final JoinType joinType;

	public JoinItem(ColumnItem leftColumn, ColumnItem rightColumn, JoinType joinType) {
		this.joinItems = new ArrayList<>();
		this.joinItems.add(new SingleColumnJoinItem(leftColumn, rightColumn));
		this.joinType = joinType;
	}

	public void addJoinColumn(ColumnItem leftColumn, ColumnItem rightColumn) {
		joinItems.add(new SingleColumnJoinItem(leftColumn, rightColumn));
	}

	public List<SingleColumnJoinItem> getJoinItems() {
		return joinItems;
	}
	
	public boolean hasJoinItem(SingleColumnJoinItem singleColumnJoinItem) {
		return hasJoinItem(singleColumnJoinItem.getLeftColumn(), singleColumnJoinItem.getRightColumn());
	}

	public boolean hasJoinItem(ColumnItem primaryColumnItem, ColumnItem foreignColumnItem) {
		for (SingleColumnJoinItem joinItem : joinItems) {
			ColumnItem leftColumnItem = joinItem.getLeftColumn();
			ColumnItem rightColumnItem = joinItem.getRightColumn();
			if ((leftColumnItem.getColumn() == primaryColumnItem.getColumn()
					&& leftColumnItem.getTableItem() == primaryColumnItem.getTableItem()
					&& rightColumnItem.getColumn() == foreignColumnItem.getColumn()
					&& rightColumnItem.getTableItem() == foreignColumnItem.getTableItem())
					|| (leftColumnItem.getColumn() == foreignColumnItem.getColumn()
							&& leftColumnItem.getTableItem() == foreignColumnItem.getTableItem()
							&& rightColumnItem.getColumn() == primaryColumnItem.getColumn()
							&& rightColumnItem.getTableItem() == primaryColumnItem.getTableItem())) {
				return true;
			}
		}
		return false;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public class SingleColumnJoinItem extends Item {

		private static final long serialVersionUID = 1L;
		private final ColumnItem leftColumn;
		private final ColumnItem rightColumn;
		private final JoinType joinType;

		public SingleColumnJoinItem(ColumnItem leftColumn, ColumnItem rightColumn) {
			this.leftColumn = leftColumn;
			this.rightColumn = rightColumn;
			this.joinType = JoinType.LEFT;
		}

		public SingleColumnJoinItem(ColumnItem leftColumn, ColumnItem rightColumn, JoinType joinType) {
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

}

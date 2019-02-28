package com.github.mengxianun.core.item;

public class GroupItem extends Item {

	private static final long serialVersionUID = 1L;
	private ColumnItem columnItem;

	public GroupItem() {
	}

	public GroupItem(ColumnItem columnItem) {
		this.columnItem = columnItem;
	}

	public ColumnItem getColumnItem() {
		return columnItem;
	}

	public void setColumnItem(ColumnItem columnItem) {
		this.columnItem = columnItem;
	}

}

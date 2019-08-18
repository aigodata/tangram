package com.github.mengxianun.core.item;

import com.github.mengxianun.core.schema.relationship.Relationship;

public class RelationshipItem {

	private final TableItem leftTableItem;
	private final TableItem rightTableItem;
	private final Relationship relationship;

	public RelationshipItem(TableItem leftTableItem, TableItem rightTableItem, Relationship relationship) {
		this.leftTableItem = leftTableItem;
		this.rightTableItem = rightTableItem;
		this.relationship = relationship;
	}

	public TableItem getLeftTableItem() {
		return leftTableItem;
	}

	public TableItem getRightTableItem() {
		return rightTableItem;
	}

	public Relationship getRelationship() {
		return relationship;
	}

}

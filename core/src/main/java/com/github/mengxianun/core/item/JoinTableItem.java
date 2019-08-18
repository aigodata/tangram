package com.github.mengxianun.core.item;

import java.util.List;

import com.github.mengxianun.core.schema.Table;

public class JoinTableItem extends TableItem {

	private static final long serialVersionUID = 1L;

	private final List<RelationshipItem> relationshipItems;

	public JoinTableItem(Table table, String alias, boolean customAlias, List<RelationshipItem> relationshipItems) {
		super(table, alias, customAlias);
		this.relationshipItems = relationshipItems;
	}

	public List<RelationshipItem> getRelationshipItems() {
		return relationshipItems;
	}

}

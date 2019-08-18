package com.github.mengxianun.core.item;

import java.util.Set;

import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.Relationship;

public class JoinTableItem extends TableItem {

	private static final long serialVersionUID = 1L;

	private final Set<Relationship> relationships;

	public JoinTableItem(Table table, String alias, boolean customAlias, Set<Relationship> relationships) {
		super(table, alias, customAlias);
		this.relationships = relationships;
	}

	public Set<Relationship> getRelationships() {
		return relationships;
	}

}

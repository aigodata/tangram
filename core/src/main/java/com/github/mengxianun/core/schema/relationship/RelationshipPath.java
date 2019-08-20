package com.github.mengxianun.core.schema.relationship;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.github.mengxianun.core.schema.Table;

/**
 * 表之间的关系集. 例
 * <ul>
 * <li>表A和表B为直接关联, 则关系集为 A-B
 * <li>表A和表B为间接关联, 如A-B-C, 则关系集为A-B, B-C
 * </ul>
 * 
 * @author mengxiangyun
 *
 */
public class RelationshipPath {

	private final Set<Relationship> relationships;

	public RelationshipPath() {
		this.relationships = new LinkedHashSet<>();
	}

	public RelationshipPath(Relationship relationship) {
		this();
		add(relationship);
	}

	public RelationshipPath(Set<Relationship> relationships) {
		this.relationships = relationships;
	}

	public void add(Relationship relationship) {
		relationships.add(relationship);
	}
	
	public void addAll(Set<Relationship> rs) {
		relationships.addAll(rs);
	}

	public void addAll(RelationshipPath path) {
		relationships.addAll(path.getRelationships());
	}

	public boolean has(Relationship relationship) {
		return relationships.contains(relationship);
	}

	public boolean has(Table table) {
		Set<Table> tables = new HashSet<>();
		for (Relationship relationship : relationships) {
			tables.add(relationship.getPrimaryColumn().getTable());
			tables.add(relationship.getForeignColumn().getTable());
		}
		return tables.contains(table);
	}

	public Relationship getFirst() {
		return relationships.iterator().next();
	}

	public Set<Relationship> getRelationships() {
		return relationships;
	}

	public int size() {
		return relationships.size();
	}

	@Override
	public int hashCode() {
		return relationships.stream().mapToInt(Relationship::hashCode).sum();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RelationshipPath)) {
			return false;
		}
		RelationshipPath other = (RelationshipPath) obj;
		if (relationships.size() != other.relationships.size()) {
			return false;
		}
		Iterator<Relationship> iterator = relationships.iterator();
		Iterator<Relationship> otherIterator = other.relationships.iterator();
		while (iterator.hasNext()) {
			boolean eq = iterator.next().equals(otherIterator.next());
			if (!eq) {
				return false;
			}
		}
		return true;
	}

}

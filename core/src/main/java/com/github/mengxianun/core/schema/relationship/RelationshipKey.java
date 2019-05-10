package com.github.mengxianun.core.schema.relationship;

import java.util.Objects;

import com.github.mengxianun.core.schema.Table;

public class RelationshipKey {

	private Table primaryTable;
	private Table foreignTable;

	public RelationshipKey(Table primaryTable, Table foreignTable) {
		this.primaryTable = primaryTable;
		this.foreignTable = foreignTable;
	}

	public Table getPrimaryTable() {
		return primaryTable;
	}

	public void setPrimaryTable(Table primaryTable) {
		this.primaryTable = primaryTable;
	}

	public Table getForeignTable() {
		return foreignTable;
	}

	public void setForeignTable(Table foreignTable) {
		this.foreignTable = foreignTable;
	}

	@Override
	public int hashCode() {
		return Objects.hash(primaryTable.getName(), primaryTable.getSchema().getName(), foreignTable.getName(),
				foreignTable.getSchema().getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof RelationshipKey)) {
			return false;
		}
		RelationshipKey key = (RelationshipKey) obj;
		return Objects.equals(primaryTable, key.getPrimaryTable())
				&& Objects.equals(primaryTable.getSchema(), key.getPrimaryTable().getSchema())
				&& Objects.equals(foreignTable, key.getForeignTable())
				&& Objects.equals(foreignTable.getSchema(), key.getForeignTable().getSchema());
	}

}

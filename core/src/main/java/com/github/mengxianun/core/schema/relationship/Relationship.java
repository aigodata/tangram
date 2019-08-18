package com.github.mengxianun.core.schema.relationship;

import java.util.Objects;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.schema.Column;

public class Relationship {

	private Column primaryColumn;
	private Column foreignColumn;
	private AssociationType associationType;

	public Relationship(Column primaryColumn, Column foreignColumn) {
		this.primaryColumn = primaryColumn;
		this.foreignColumn = foreignColumn;
		this.associationType = AssociationType.ONE_TO_ONE;
	}

	public Relationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		this.primaryColumn = primaryColumn;
		this.foreignColumn = foreignColumn;
		this.associationType = associationType;
	}

	public Column getPrimaryColumn() {
		return primaryColumn;
	}

	public void setPrimaryColumn(Column primaryColumn) {
		this.primaryColumn = primaryColumn;
	}

	public Column getForeignColumn() {
		return foreignColumn;
	}

	public void setForeignColumn(Column foreignColumn) {
		this.foreignColumn = foreignColumn;
	}

	public AssociationType getAssociationType() {
		return associationType;
	}

	public void setAssociationType(AssociationType associationType) {
		this.associationType = associationType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(primaryColumn.getName(), primaryColumn.getTable().getName(),
				primaryColumn.getTable().getSchema().getName(), foreignColumn.getName(),
				foreignColumn.getTable().getName(), foreignColumn.getTable().getSchema().getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof Relationship)) {
			return false;
		}
		Relationship relationship = (Relationship) obj;
		return Objects.equals(primaryColumn, relationship.getPrimaryColumn())
				&& Objects.equals(foreignColumn, relationship.getForeignColumn());
	}

}

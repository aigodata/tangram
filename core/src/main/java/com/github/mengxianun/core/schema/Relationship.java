package com.github.mengxianun.core.schema;

import java.util.List;

import com.github.mengxianun.core.attributes.AssociationType;

public interface Relationship {

	public Table getPrimaryTable();

	public List<Column> getPrimaryColumns();

	public Table getForeignTable();

	public List<Column> getForeignColumns();

	public AssociationType getAssociationType();

	public void addRelation(Column primaryColumn, Column foreignColumn, AssociationType associationType);

}

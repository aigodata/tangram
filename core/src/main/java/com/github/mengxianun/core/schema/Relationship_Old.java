package com.github.mengxianun.core.schema;

import com.github.mengxianun.core.attributes.AssociationType;

@Deprecated
public interface Relationship_Old {

	public Column getPrimaryColumn();

	public Column getForeignColumn();

	public AssociationType getAssociationType();

}

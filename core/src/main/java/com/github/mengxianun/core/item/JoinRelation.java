package com.github.mengxianun.core.item;

import java.util.List;

import com.github.mengxianun.core.json.JoinType;
import com.github.mengxianun.core.schema.Table;

/**
 * 关联关系结构
 * 
 * @author mengxiangyun
 *
 */
public class JoinRelation {

	private Table table;
	private JoinType joinType;
	private List<JoinRelation> subJoinRelations;

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	public JoinType getJoinType() {
		return joinType;
	}

	public void setJoinType(JoinType joinType) {
		this.joinType = joinType;
	}

	public List<JoinRelation> getSubJoinRelations() {
		return subJoinRelations;
	}

	public void setSubJoinRelations(List<JoinRelation> subJoinRelations) {
		this.subJoinRelations = subJoinRelations;
	}

}

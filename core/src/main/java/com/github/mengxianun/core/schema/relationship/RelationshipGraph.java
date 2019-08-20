package com.github.mengxianun.core.schema.relationship;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;

public final class RelationshipGraph {

	// 两个表之间的关联关系, 集合中存储的是直接的关联关系, 即A-B, B-C, 而不是A-C
	private final com.google.common.collect.Table<Table, Table, Set<Relationship>> relationships = HashBasedTable
			.create();

	private final LoadingCache<RelationshipKey, Set<RelationshipPath>> relationshipsCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterWrite(60, TimeUnit.MINUTES)
			.build(new CacheLoader<RelationshipKey, Set<RelationshipPath>>() {

				public Set<RelationshipPath> load(RelationshipKey key) throws Exception {
					Set<RelationshipPath> paths = new LinkedHashSet<>();
					findAllPaths(new RelationshipPath(), paths, key.getPrimaryTable(), key.getForeignTable());
					return paths;
				}
			});

	/**
	 * 主表和外表为直接关联关系
	 * 
	 * @param primaryColumn
	 * @param foreignColumn
	 * @param associationType
	 */
	public boolean addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		Table primaryTable = primaryColumn.getTable();
		Table foreignTable = foreignColumn.getTable();

		Relationship relationship = new Relationship(primaryColumn, foreignColumn, associationType);
		Set<Relationship> ships;
		if (relationships.contains(primaryTable, foreignTable)) {
			ships = relationships.get(primaryTable, foreignTable);
			if (ships.contains(relationship)) {
				return false;
			}
		} else {
			ships = new LinkedHashSet<>();
		}
		ships.add(relationship);
		relationships.put(primaryTable, foreignTable, ships);
		// 清空缓存
		relationshipsCache.cleanUp();

		return true;
	}

	public Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable) {
		try {
			return relationshipsCache.get(new RelationshipKey(primaryTable, foreignTable));
		} catch (ExecutionException e) {
			return Collections.emptySet();
		}
	}

	private void findAllPaths(RelationshipPath visited, Set<RelationshipPath> paths, Table currentTable,
			Table targetTable) {
		if (currentTable == targetTable) {
			paths.add(visited);
			return;
		}
		Map<Table, Set<Relationship>> row = relationships.row(currentTable);

		for (Entry<Table, Set<Relationship>> entry : row.entrySet()) {
			Table node = entry.getKey();
			for (Relationship relationship : entry.getValue()) {
				if (visited.has(relationship.getForeignColumn().getTable())) {
					continue;
				}
				RelationshipPath temp = new RelationshipPath();
				temp.addAll(visited);
				temp.add(relationship);
				findAllPaths(temp, paths, node, targetTable);
			}
		}
	}

}

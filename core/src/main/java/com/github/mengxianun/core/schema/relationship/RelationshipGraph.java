package com.github.mengxianun.core.schema.relationship;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Table;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;

public final class RelationshipGraph {

	private static final Logger logger = LoggerFactory.getLogger(RelationshipGraph.class);

	// 两个表之间的关联关系, 集合中存储的是直接的关联关系, 即A-B, B-C, 而不是A-C
	private final com.google.common.collect.Table<Table, Table, Set<Relationship>> relationships = HashBasedTable
			.create();

	private final LoadingCache<RelationshipKey, Set<RelationshipPath>> relationshipsCache = CacheBuilder.newBuilder()
			.maximumSize(10000).expireAfterAccess(24, TimeUnit.HOURS)
			.build(new CacheLoader<RelationshipKey, Set<RelationshipPath>>() {

				public Set<RelationshipPath> load(RelationshipKey key) throws Exception {
					Set<RelationshipPath> paths = new LinkedHashSet<>();
					findAllPaths(new RelationshipPath(), paths, key.getPrimaryTable(), key.getForeignTable(), true);
					return paths;
				}
			});

	public Set<Relationship> getAllRelationships() {
		return relationships.cellSet().stream().flatMap(e -> e.getValue().stream()).collect(Collectors.toSet());
	}

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

		logger.info("Add relationship [{}.{}] <-> [{}.{}]", primaryColumn.getTable().getName(), primaryColumn.getName(),
				foreignColumn.getTable().getName(), foreignColumn.getName());
		return true;
	}

	public boolean hasRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		Table primaryTable = primaryColumn.getTable();
		Table foreignTable = foreignColumn.getTable();
		if (relationships.contains(primaryTable, foreignTable)) {
			Set<Relationship> ships = relationships.get(primaryTable, foreignTable);
			Relationship relationship = new Relationship(primaryColumn, foreignColumn, associationType);
			if (ships.contains(relationship)) {
				return true;
			}
		}
		return false;
	}

	public boolean deleteRelationship(Column primaryColumn, Column foreignColumn) {
		boolean result = false;
		Relationship relationship = new Relationship(primaryColumn, foreignColumn);
		Table primaryTable = primaryColumn.getTable();
		Table foreignTable = foreignColumn.getTable();
		if (relationships.contains(primaryTable, foreignTable)) {
			Set<Relationship> ships = relationships.get(primaryTable, foreignTable);
			Iterator<Relationship> iterator = ships.iterator();
			while (iterator.hasNext()) {
				Relationship ship = iterator.next();
				if (ship.equals(relationship)) {
					ships.remove(ship);
					result = true;
					break;
				}
			}
		}
		logger.info("Delete relationship [{}.{}] <-> [{}.{}] {}", primaryColumn.getTable().getName(),
				primaryColumn.getName(), foreignColumn.getTable().getName(), foreignColumn.getName(), result);
		return result;
	}

	public boolean deleteRelationship(Table primaryTable, Table foreignTable) {
		boolean result = relationships.remove(primaryTable, foreignTable) != null
				|| relationships.remove(foreignTable, primaryTable) != null;
		logger.info("Delete relationship [{}] <-> [{}] {}", primaryTable.getName(), primaryTable.getName(), result);
		return result;
	}

	public Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable) {
		/**
		 * temporarily
		 */
		Set<RelationshipPath> paths = new LinkedHashSet<>();
		findAllPaths(new RelationshipPath(), paths, primaryTable, foreignTable, true);
		return paths;
		//		RelationshipKey relationshipKey = new RelationshipKey(primaryTable, foreignTable);
		//		try {
		//			return relationshipsCache.get(relationshipKey);
		//		} catch (ExecutionException e) {
		//			String message = String.format("Get association relation error for the table [%s] and [%s]",
		//					primaryTable.getName(), foreignTable.getName());
		//			logger.error(message, e);
		//			//			relationshipsCache.invalidate(relationshipKey);
		//			Set<RelationshipPath> paths = new LinkedHashSet<>();
		//			findAllPaths(new RelationshipPath(), paths, primaryTable, foreignTable, true);
		//			return paths;
		//		}
	}

	private void findAllPaths(RelationshipPath visited, Set<RelationshipPath> paths, Table currentTable,
			Table targetTable, boolean begin) {
		// 'begin' is to avoid self-associated tables
		if (currentTable == targetTable && !begin) {
			paths.add(visited);
			return;
		}
		begin = false;
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
				findAllPaths(temp, paths, node, targetTable, begin);
			}
		}
	}

	public void cleanRelationship() {
		// 清空缓存
		relationshipsCache.invalidateAll();
	}

}

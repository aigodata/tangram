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
import com.github.mengxianun.core.schema.DefaultColumn;
import com.github.mengxianun.core.schema.DefaultSchema;
import com.github.mengxianun.core.schema.DefaultTable;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.HashBasedTable;

public final class RelationshipGraph {

	// 两个表之间的关联关系, 集合中存储的是直接的关联关系
	private final com.google.common.collect.Table<Table, Table, Set<Relationship>> relationships = HashBasedTable
			.create();

	private final LoadingCache<RelationshipKey, Set<RelationshipPath>> relationshipsCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterWrite(60, TimeUnit.MINUTES)
			.build(new CacheLoader<RelationshipKey, Set<RelationshipPath>>() {

				public Set<RelationshipPath> load(RelationshipKey key) throws Exception {
					return findRelationships(key.getPrimaryTable(), key.getForeignTable());
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

		// Refresh the cache
		relationshipsCache.refresh(new RelationshipKey(primaryTable, foreignTable));

		return true;
	}

	public Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable) {
		try {
			return relationshipsCache.get(new RelationshipKey(primaryTable, foreignTable));
		} catch (ExecutionException e) {
			return Collections.emptySet();
		}
	}

	private Set<RelationshipPath> findRelationships(Table primaryTable, Table foreignTable) {
		Set<RelationshipPath> paths = new LinkedHashSet<>();
		if (relationships.contains(primaryTable, foreignTable)) {
			Set<Relationship> ships = relationships.get(primaryTable, foreignTable);
			// 每个直接的关联关系是一个单独的路径
			for (Relationship relationship : ships) {
				RelationshipPath relationshipPath = new RelationshipPath(relationship);
				paths.add(relationshipPath);
			}
			return paths;
		}
		RelationshipPath visited = new RelationshipPath();
		findAllPaths(visited, paths, primaryTable, foreignTable);
		return paths;
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

	public static void main(String[] args) {
		Schema schema = new DefaultSchema("test");

		DefaultTable tableA = new DefaultTable("a", null, schema);
		DefaultTable tableB = new DefaultTable("b", null, schema);
		DefaultTable tableC = new DefaultTable("c", null, schema);
		DefaultTable tableD = new DefaultTable("d", null, schema);

		DefaultColumn columnAX = new DefaultColumn(tableA, "x");
		DefaultColumn columnAY = new DefaultColumn(tableA, "y");
		DefaultColumn columnAZ = new DefaultColumn(tableA, "z");

		DefaultColumn columnBX = new DefaultColumn(tableB, "x");
		DefaultColumn columnBY = new DefaultColumn(tableB, "y");
		DefaultColumn columnBZ = new DefaultColumn(tableB, "z");

		DefaultColumn columnCX = new DefaultColumn(tableC, "x");
		DefaultColumn columnCY = new DefaultColumn(tableC, "y");
		DefaultColumn columnCZ = new DefaultColumn(tableC, "z");

		DefaultColumn columnDX = new DefaultColumn(tableD, "x");
		DefaultColumn columnDY = new DefaultColumn(tableD, "y");
		DefaultColumn columnDZ = new DefaultColumn(tableD, "z");

		RelationshipGraph graph = new RelationshipGraph();

		graph.addRelationship(columnAX, columnBX, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnAY, columnBX, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnBX, columnCX, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnBY, columnCX, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnCX, columnDY, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnCX, columnBZ, AssociationType.ONE_TO_ONE);
		graph.addRelationship(columnCZ, columnAY, AssociationType.ONE_TO_ONE);

		print(graph, tableA, tableB);
		System.out.println("-----------------------");
		print(graph, tableA, tableC);
		System.out.println("-----------------------");
		print(graph, tableA, tableD);
		System.out.println("-----------------------");
		print(graph, tableB, tableC);
		System.out.println("-----------------------");
		print(graph, tableB, tableD);
		System.out.println("-----------------------");
		print(graph, tableC, tableD);
		System.out.println("-----------------------");

	}

	public static void print(RelationshipGraph graph, DefaultTable table1, DefaultTable table2) {
		System.out.println("Relationship:" + table1.getName() + "-" + table2.getName());
		Set<RelationshipPath> paths = graph.getRelationships(table1, table2);
		for (RelationshipPath relationshipPath : paths) {
			Set<Relationship> ships = relationshipPath.getRelationships();
			for (Relationship relationship : ships) {
				Column primaryColumn = relationship.getPrimaryColumn();
				Column foreignColumn = relationship.getForeignColumn();
				System.out.print(primaryColumn.getTable().getName() + ":" + primaryColumn.getName() + "-"
						+ foreignColumn.getTable().getName() + ":" + foreignColumn.getName());
				System.out.print(" ");
			}
			System.out.println();
		}
	}

}

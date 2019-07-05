package com.github.mengxianun.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.mengxianun.core.attributes.AssociationType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.RelationshipKey;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.JsonElement;

public abstract class AbstractDataContext implements DataContext {

	protected Metadata metadata = new Metadata();

	protected Dialect dialect;
	// Key 为主表, Value 为 Map 类型, Key为外表, 值为主外表的关联关系
	Map<Table, Map<Table, Set<Relationship>>> pfRelationships = new HashMap<>();
	// 表关联关系缓存, Key 为两个表的对象
	LoadingCache<RelationshipKey, Set<Relationship>> relationshipsCache = CacheBuilder.newBuilder()
			.maximumSize(1000).expireAfterWrite(60, TimeUnit.MINUTES)
			.build(new CacheLoader<RelationshipKey, Set<Relationship>>() {

				public Set<Relationship> load(RelationshipKey key) throws Exception {
					return findRelationships(key.getPrimaryTable(), key.getForeignTable());
				}
			});

	protected abstract void initializeMetadata();

	@Override
	public JsonElement executeNative(Table table, String script) {
		return executeNative(script);
	}

	@Override
	public List<Schema> getSchemas() {
		return metadata.getSchemas();
	}

	@Override
	public Schema getDefaultSchema() {
		return metadata.getDefaultSchema();
	}

	@Override
	public Schema getSchema(String schemaName) {
		return metadata.getSchema(schemaName);
	}

	@Override
	public Table getTable(String tableName) {
		return metadata.getTable(tableName);
	}

	@Override
	public Table getTable(String schemaName, String tableName) {
		return metadata.getTable(schemaName, tableName);
	}

	@Override
	public Column getColumn(String tableName, String columnName) {
		return metadata.getColumn(tableName, columnName);
	}

	@Override
	public Column getColumn(String schemaName, String tableName, String columnName) {
		return metadata.getColumn(schemaName, tableName, columnName);
	}

	@Override
	public String getIdentifierQuoteString() {
		return metadata.getIdentifierQuoteString();
	}

	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public void destroy() {
		// TODO
	}

	@Override
	public void addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		Table primaryTable = primaryColumn.getTable();
		Table foreignTable = foreignColumn.getTable();
		//
		Map<Table, Set<Relationship>> fRelationships = null;
		if (pfRelationships.containsKey(primaryTable)) {
			fRelationships = pfRelationships.get(primaryTable);
		} else {
			fRelationships = new HashMap<>();
			pfRelationships.put(primaryTable, fRelationships);
		}
		//
		Set<Relationship> relationships = null;
		if (fRelationships.containsKey(foreignTable)) {
			relationships = fRelationships.get(foreignTable);
		} else {
			relationships = new LinkedHashSet<>();
			fRelationships.put(foreignTable, relationships);
		}
		Relationship relationship = new Relationship(primaryColumn, foreignColumn, associationType);
		if (!relationships.contains(relationship)) {
			relationships.add(relationship);
		}
	}

	@Override
	public Set<Relationship> getRelationships(Table primaryTable, Table foreignTable) {
		try {
			return relationshipsCache.get(new RelationshipKey(primaryTable, foreignTable));
		} catch (ExecutionException e) {
			return Collections.emptySet();
		}
	}

	public Set<Relationship> findRelationships(Table primaryTable, Table foreignTable) {
		Set<Relationship> relationships = new LinkedHashSet<>();
		// 要查找的主表没有关联关系
		if (!pfRelationships.containsKey(primaryTable)) {
			return Collections.emptySet();
		}
		Map<Table, Set<Relationship>> fRelationships = pfRelationships.get(primaryTable);
		// 要查找的主表和外表有直接的关联关系
		if (fRelationships.containsKey(foreignTable)) {
			return fRelationships.get(foreignTable);
		} else {
			// 循环主表的所有关联表, 然后以关联表为新的主表向下寻找关联, 直到找到最开始要找的关联关系
			for (Table fTable : fRelationships.keySet()) {
				Set<Relationship> subRelationships = findSubRelationships(fTable, foreignTable, primaryTable,
						relationships, new HashSet<>());
				if (!subRelationships.isEmpty()) { // 找到了关联关系
					// 添加前两个表的关联关系
					Set<Relationship> firstRelationships = findRelationships(primaryTable, fTable);
					relationships.addAll(firstRelationships);
					// 添加后续的关联关系
					relationships.addAll(subRelationships);
					break;
				}
			}
		}
		return relationships;
	}

	public Set<Relationship> findSubRelationships(Table primaryTable, Table foreignTable, Table originalTable,
			Set<Relationship> relationships, Set<Table> pastTables) {
		// 在未找到关联表之前找到了开始主表, 形成了死循环
		if (originalTable == primaryTable) {
			return Collections.emptySet();
		}
		// 找不到主表的关联关系
		if (!pfRelationships.containsKey(primaryTable)) {
			return Collections.emptySet();
		}
		Map<Table, Set<Relationship>> fRelationships = pfRelationships.get(primaryTable);
		if (fRelationships.containsKey(foreignTable)) {
			return fRelationships.get(foreignTable);
		} else {
			for (Table fTable : fRelationships.keySet()) {
				if (pastTables.contains(fTable)) {
					continue;
				}
				pastTables.add(fTable);
				Set<Relationship> subRelationships = findSubRelationships(fTable, foreignTable, originalTable,
						relationships, pastTables);
				if (!subRelationships.isEmpty()) { // 找到了关联关系
					// 添加前两个表的关联关系
					Set<Relationship> firstRelationships = findRelationships(primaryTable, fTable);
					relationships.addAll(firstRelationships);
					// 添加后续的关联关系
					relationships.addAll(subRelationships);
				}
			}
		}
		return relationships;
	}

	@Override
	public AssociationType getAssociationType(Table primaryTable, Table foreignTable) {
		Set<Relationship> relationships = getRelationships(primaryTable, foreignTable);
		Relationship first = Iterables.getFirst(relationships, null);
		AssociationType associationType = first.getAssociationType();
		Relationship last = Iterables.getLast(relationships, null);
		if (last != null) {
			AssociationType lastAssociationType = last.getAssociationType();
			if (associationType == AssociationType.ONE_TO_ONE || associationType == AssociationType.ONE_TO_MANY) {
				if (lastAssociationType == AssociationType.ONE_TO_MANY || lastAssociationType == AssociationType.MANY_TO_MANY) {
					associationType = AssociationType.ONE_TO_MANY;
				} else {
					associationType = AssociationType.ONE_TO_ONE;
				}
			} else if (associationType == AssociationType.MANY_TO_ONE
					|| associationType == AssociationType.MANY_TO_MANY) {
				if (lastAssociationType == AssociationType.ONE_TO_MANY || lastAssociationType == AssociationType.MANY_TO_MANY) {
					associationType = AssociationType.MANY_TO_MANY;
				} else {
					associationType = AssociationType.MANY_TO_ONE;
				}
			}
		}
		return associationType;
	}

}

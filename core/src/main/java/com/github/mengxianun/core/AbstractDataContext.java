package com.github.mengxianun.core;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.data.DataSet;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.update.UpdateSummary;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.render.JsonRenderer;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.resutset.DataResult;
import com.github.mengxianun.core.resutset.DefaultDataResult;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Relationship;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.RelationshipKey;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractDataContext implements DataContext {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDataContext.class);

	protected final Metadata metadata = new Metadata();

	protected Dialect dialect;
	protected SQLBuilder sqlBuilder;
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

	protected abstract void initMetadata();

	@Override
	public DataResult execute(Action action) {
		DataResult resultSet = null;
		if (action.isStruct()) {
			resultSet = executeStruct(action);
		} else if (action.isStructs()) {
			resultSet = executeStructs(action);
		} else if (action.isTransaction()) {
			resultSet = executeTransaction(action);
		} else if (action.isNative()) {
			Operation operation = action.getOperation();
			String resource = action.getTableItems().get(0).getExpression();
			String nativeContent = action.getNativeContent();
			resultSet = executeNative(operation, resource, nativeContent);
		} else if (action.isCRUD()) {
			resultSet = executeCRUD(action);
		} else {
			throw new UnsupportedOperationException(action.getOperation().name());
		}
		return resultSet;
	}

	@Override
	public List<DataResult> execute(Action... actions) {
		List<DataResult> multiResults = new ArrayList<>();
		trans(new Atom() {

			@Override
			public void run() {
				for (Action action : actions) {
					multiResults.add(executeCRUD(action));
				}

			}
		});
		return multiResults;
	}

	protected abstract void trans(Atom... atoms);

	private DataResult executeStruct(Action action) {
		TableItem tableItem = action.getTableItems().get(0);
		Table table = tableItem.getTable();
		return new DefaultDataResult(table.getInfo());
	}

	private DataResult executeStructs(Action action) {
		Schema schema = App.currentDataContext().getDefaultSchema();
		return new DefaultDataResult(schema.getInfo());
	}

	private DataResult executeTransaction(Action action) {
		JsonObject jsonData = action.getRequestData();
		JsonArray transactionArray = jsonData.getAsJsonArray(Operation.TRANSACTION.name().toLowerCase());
		List<Action> actions = new ArrayList<>();
		for (int i = 0; i < transactionArray.size(); i++) {
			JsonObject innerJsonData = transactionArray.get(i).getAsJsonObject();
			JsonParser innerJsonParser = new JsonParser(innerJsonData);
			Action innerAction = innerJsonParser.parse();
			innerAction.build();
			actions.add(innerAction);
		}
		List<DataResult> dataResults = execute(actions.toArray(new Action[] {}));
		List<Object> multiResult = dataResults.stream().map(DataResult::getData).collect(Collectors.toList());
		return new DefaultDataResult(multiResult);
	}

	protected DataResult executeCRUD(Action action) {
		DataResult resultSet = null;
		String sql = action.getSql();
		Object[] params = action.getParams().toArray();

		logger.debug("SQL: {}", sql);
		logger.debug("Params: {}", params);

		if (action.isQuery()) {
			resultSet = new DefaultDataResult(query(action));
		} else if (action.isInsert()) {
			resultSet = new DefaultDataResult(insert(action));
		} else if (action.isUpdate() || action.isDelete()) {
			resultSet = new DefaultDataResult(update(action));
		}
		return resultSet;
	}

	private Object query(Action action) {
		DataSet dataSet = select(action.getSql(), action.getParams().toArray());
		Object result = render(dataSet.toRows(), action);
		return processQuery(result, action);
	}

	private Object processQuery(Object result, Action action) {
		if (action.isLimit()) {
			result = wrapPageResult(result, action);
		}
		return result;
	}

	private Object render(List<Row> rows, Action action) {
		JsonArray jsonArrayData = new JsonRenderer(action).render(rows);
		JsonElement jsonData = jsonArrayData;
		if (action.isDetail()) {
			if (jsonArrayData.size() > 0) {
				jsonData = jsonArrayData.get(0);
			} else {
				jsonData = new JsonObject();
			}
		}
		return getNativeObject(jsonData);
	}

	private Object getNativeObject(JsonElement jsonData) {
		if (jsonData.isJsonArray()) {
			Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
			return new Gson().fromJson(jsonData, dataType);
		} else if (jsonData.isJsonObject()) {
			Type dataType = new TypeToken<Map<String, Object>>() {}.getType();
			return new Gson().fromJson(jsonData, dataType);
		} else {
			Type dataType = new TypeToken<Object>() {}.getType();
			return new Gson().fromJson(jsonData, dataType);
		}
	}

	private Map<String, Object> wrapPageResult(Object result, Action action) {
		LimitItem limitItem = action.getLimitItem();
		long start = limitItem.getStart();
		long end = limitItem.getEnd();

		Action countAction = action.count();
		DataSet countDataSet = select(countAction.getSql(), countAction.getParams().toArray());
		Row row = countDataSet.getRow();
		long count = new Double(row.getValue(0).toString()).longValue();
		Map<String, Object> pageResult = new LinkedHashMap<>();
		pageResult.put(ResultAttributes.START, start);
		pageResult.put(ResultAttributes.END, end);
		pageResult.put(ResultAttributes.TOTAL, count);
		pageResult.put(ResultAttributes.DATA, result);
		return pageResult;
	}

	@Override
	public DataResult executeSql(String sql, Object... params) {
		logger.debug("SQL: {}", sql);
		logger.debug("Params: {}", params);

		sql = sql.trim();
		if (sql.toUpperCase().startsWith("SELECT")) {
			return new DefaultDataResult(select(sql, params));
		} else if (sql.toUpperCase().startsWith("INSERT")) {
			return new DefaultDataResult(insert(sql, params));
		} else if (sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")) {
			return new DefaultDataResult(update(sql, params));
		}
		throw new UnsupportedOperationException();
	}

	protected DataSet select(Action action) {
		return select(action.getSql(), action.getParams().toArray());
	}

	protected UpdateSummary insert(Action action) {
		return insert(action.getSql(), action.getParams().toArray());
	}

	protected UpdateSummary update(Action action) {
		return update(action.getSql(), action.getParams().toArray());
	}

	protected abstract DataSet select(String sql, Object... params);

	protected abstract UpdateSummary insert(String sql, Object... params);

	protected abstract UpdateSummary update(String sql, Object... params);

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

	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public SQLBuilder getSQLBuilder(Action action) {
		return new SQLBuilder(action);
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
		if (associationType == AssociationType.ONE_TO_MANY || associationType == AssociationType.MANY_TO_MANY) {
			return associationType;
		}
		if (relationships.size() > 1) {
			Relationship last = Iterables.getLast(relationships, null);
			AssociationType lastAssociationType = last.getAssociationType();
			if (associationType == AssociationType.ONE_TO_ONE && (lastAssociationType == AssociationType.ONE_TO_MANY
					|| lastAssociationType == AssociationType.MANY_TO_MANY)) {
					associationType = AssociationType.ONE_TO_MANY;
			} else if (associationType == AssociationType.MANY_TO_ONE
					&& (lastAssociationType == AssociationType.ONE_TO_MANY
							|| lastAssociationType == AssociationType.MANY_TO_MANY)) {
					associationType = AssociationType.MANY_TO_MANY;
			}
		}
		return associationType;
	}

}

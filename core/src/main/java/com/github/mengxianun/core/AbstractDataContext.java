package com.github.mengxianun.core;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mengxianun.core.config.AssociationType;
import com.github.mengxianun.core.config.ResultAttributes;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.data.Summary;
import com.github.mengxianun.core.data.summary.BasicSummary;
import com.github.mengxianun.core.data.summary.InsertSummary;
import com.github.mengxianun.core.data.summary.MultiSummary;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.github.mengxianun.core.data.summary.UpdateSummary;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.item.ValuesItem;
import com.github.mengxianun.core.render.JsonRenderer;
import com.github.mengxianun.core.request.Operation;
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
	public Summary execute(Action action) {
		Summary summary = null;
		if (action.isStruct()) {
			summary = executeStruct(action);
		} else if (action.isStructs()) {
			summary = executeStructs(action);
		} else if (action.isTransaction()) {
			summary = executeTransaction(action);
		} else if (action.isSQL()) {
			return executeSql(action.getNativeSQL());
		} else if (action.isNative()) {
			summary = executeNative(action.getNativeContent());
		} else if (action.isCRUD()) {
			summary = executeCRUD(action);
		} else {
			throw new UnsupportedOperationException(action.getOperation().name());
		}
		return summary;
	}

	@Override
	public MultiSummary execute(Action... actions) {
		List<Summary> summaries = new ArrayList<>();
		trans(new Atom() {

			@Override
			public void run() {
				for (Action action : actions) {
					boolean parsed = parsePlaceholder(action, summaries);
					if (parsed) {
						action.reBuild();
					}
					summaries.add(executeCRUD(action));
				}

			}
		});
		return new MultiSummary(summaries);
	}

	protected abstract void trans(Atom... atoms);

	private boolean parsePlaceholder(Action action, List<Summary> summaries) {
		boolean parsed = false;
		List<? extends ValuesItem> valuesItems = Stream.of(action.getFilterItems(), action.getValueItems())
				.flatMap(List::stream).collect(Collectors.toList());
		for (ValuesItem valuesItem : valuesItems) {
			Object value = valuesItem.getValue();
			if (!(value instanceof String) || value.toString().equals("")) {
				continue;
			}
			String valueString = value.toString();
			// $n.column
			String patternString = "^[$]\\d+\\..+";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(valueString);
			if (matcher.matches()) {
				parsed = true;
				String[] numAndColumn = matcher.group().split("\\.");
				int num = Integer.parseInt(numAndColumn[0].substring(1)) - 1;
				String columnName = numAndColumn[1];
				Summary preSummary = summaries.get(num);

				Object parseValue = parsePlaceholder(preSummary, columnName);
				valuesItem.setValue(parseValue);
			}
		}
		return parsed;
	}

	private Object parsePlaceholder(Summary summary, String columnName) {
		boolean parsed = false;
		Object parseValue = null;
		if (summary instanceof QuerySummary) {
			QuerySummary querySummary = (QuerySummary) summary;
			List<Map<String, Object>> values = querySummary.getValues();
			// 暂时只获取第一条
			Map<String, Object> rowData = values.get(0);
			if (rowData.containsKey(columnName)) {
				parsed = true;
				parseValue = rowData.get(columnName);
			}

		} else if (summary instanceof InsertSummary) {
			InsertSummary insertSummary = (InsertSummary) summary;
			List<Map<String, Object>> values = insertSummary.getValues();
			// 暂时只获取第一条
			Map<String, Object> rowData = values.get(0);
			if (rowData.containsKey(columnName)) {
				parsed = true;
				parseValue = rowData.get(columnName);
			}
		} else if (summary instanceof UpdateSummary) {
			UpdateSummary updateSummary = (UpdateSummary) summary;
			if (ResultAttributes.COUNT.equals(columnName)) {
				parsed = true;
				parseValue = updateSummary.getUpdateCount();
			}
		}
		if (!parsed) {
			throw new DataException("Placeholder [%s] parse failed", columnName);
		}
		return parseValue;
	}

	private Summary executeStruct(Action action) {
		TableItem tableItem = action.getTableItems().get(0);
		Table table = tableItem.getTable();
		return new BasicSummary(table.getInfo());
	}

	private Summary executeStructs(Action action) {
		Schema schema = action.getDataContext().getDefaultSchema();
		return new BasicSummary(schema.getInfo());
	}

	private MultiSummary executeTransaction(Action action) {
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
		return execute(actions.toArray(new Action[] {}));
	}

	protected Summary executeCRUD(Action action) {
		logger.debug("SQL: {}", action.getSql());
		logger.debug("Params: {}", action.getParams().toArray());

		Summary summary = null;
		if (action.isQuery()) {
			summary = query(action);
		} else if (action.isInsert()) {
			summary = insert(action);
		} else {
			summary = update(action);
		}
		return summary;
	}

	protected QuerySummary query(Action action) {
		QuerySummary querySummary = select(action);
		// Render
		JsonArray jsonData = new JsonRenderer(action).render(querySummary.toRows());
		// Convert json to native type
		Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
		List<Map<String, Object>> values = new Gson().fromJson(jsonData, dataType);
		querySummary.setValues(values);
		if (action.isLimit() && querySummary.getTotal() == -1) {
			long total = count(action);
			querySummary.setTotal(total);
		}
		return querySummary;
	}

	protected long count(Action action) {
		Action countAction = action.count();
		QuerySummary countSummary = select(countAction);
		Row row = countSummary.toRows().get(0);
		return new Double(row.getValue(0).toString()).longValue();
	}

	@Override
	public Summary executeSql(String sql) {
		logger.debug("Execute SQL: {}", sql);

		sql = sql.trim();
		if (sql.toUpperCase().startsWith("SELECT")) {
			return select(sql);
		} else if (sql.toUpperCase().startsWith("INSERT")) {
			return insert(sql);
		} else if (sql.toUpperCase().startsWith("UPDATE") || sql.toUpperCase().startsWith("DELETE")) {
			return update(sql);
		}
		throw new UnsupportedOperationException();
	}

	protected abstract QuerySummary select(Action action);

	protected abstract InsertSummary insert(Action action);

	protected abstract UpdateSummary update(Action action);

	protected abstract QuerySummary select(String sql);

	protected abstract InsertSummary insert(String sql);

	protected abstract UpdateSummary update(String sql);

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

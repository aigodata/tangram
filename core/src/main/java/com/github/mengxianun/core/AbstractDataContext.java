package com.github.mengxianun.core;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.github.mengxianun.core.data.summary.FileSummary;
import com.github.mengxianun.core.data.summary.InsertSummary;
import com.github.mengxianun.core.data.summary.MultiSummary;
import com.github.mengxianun.core.data.summary.QuerySummary;
import com.github.mengxianun.core.data.summary.UpdateSummary;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.item.ValuesItem;
import com.github.mengxianun.core.render.FileRenderer;
import com.github.mengxianun.core.render.JsonRenderer;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.Schema;
import com.github.mengxianun.core.schema.Table;
import com.github.mengxianun.core.schema.relationship.Relationship;
import com.github.mengxianun.core.schema.relationship.RelationshipGraph;
import com.github.mengxianun.core.schema.relationship.RelationshipPath;
import com.google.common.collect.Iterables;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

public abstract class AbstractDataContext implements DataContext {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDataContext.class);

	protected Schema schema;

	protected Dialect dialect;
	protected SQLBuilder sqlBuilder;
	private final RelationshipGraph graph = new RelationshipGraph();

	protected abstract void initMetadata();

	@Override
	public Summary execute(NewAction action) {
		Summary summary = null;
		if (action instanceof Action) {
			summary = executeCRUD((Action) action);
		} else {
			summary = action.execute();
		}
		return summary;
	}

	@Override
	public MultiSummary execute(NewAction... actions) {
		List<Summary> summaries = new ArrayList<>();
		trans(new Atom() {

			@Override
			public void run() {
				for (NewAction action : actions) {
					if (action instanceof Action) {
						Action curdAction = (Action) action;
						boolean parsed = parsePlaceholder(curdAction, summaries);
						if (parsed) {
							curdAction.reBuild();
						}
						summaries.add(curdAction.execute());
					} else {
						summaries.add(action.execute());
					}
				}

			}
		});
		return new MultiSummary(summaries);
	}

	protected abstract void trans(Atom... atoms);

	private boolean parsePlaceholder(Action action, List<Summary> summaries) {
		boolean parsed = false;
		List<? extends ValuesItem> valuesItems = Stream
				.of(action.getFilterItems(),
						action.getInsertValueItems().stream().flatMap(List::stream).collect(Collectors.toList()),
						action.getUpdateValueItem())
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

	protected Summary executeCRUD(Action action) {
		logger.debug("SQL: {}", action.getSql());
		logger.debug("Params: {}", action.getParams());

		Summary summary = null;
		if (action.isFile()) {
			summary = toFile(action);
		} else if (action.isQuery()) {
			summary = query(action);
		} else if (action.isInsert()) {
			summary = insert(action);
		} else {
			summary = update(action);
		}
		return summary;
	}

	private FileSummary toFile(Action action) {
		if (!action.isQuery()) {
			throw new UnsupportedOperationException();
		}
		QuerySummary querySummary = select(action);
		ByteArrayOutputStream outputStream = new FileRenderer(action).render(querySummary.toRows());
		return new FileSummary(querySummary, outputStream);
	}

	protected QuerySummary query(Action action) {
		QuerySummary querySummary = select(action);
		// Render
		JsonArray jsonData = new JsonRenderer(action).render(querySummary.toRows());
		// Convert json to native type
		Type dataType = new TypeToken<List<Map<String, Object>>>() {}.getType();
		List<Map<String, Object>> values = App.gson().fromJson(jsonData, dataType);
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
	public Schema getSchema() {
		return schema;
	}

	@Override
	public Table getTable(String nameOrAlias) {
		Table table = schema.getTable(nameOrAlias);
		if (table == null) {
			table = loadTable(nameOrAlias);
			schema.addTable(table);
		}
		return table;
	}

	@Override
	public Column getColumn(String tableNameOrAlias, String columnNameOrAlias) {
		Table table = schema.getTable(tableNameOrAlias);
		if (table == null) {
			return null;
		}
		return table.getColumn(columnNameOrAlias);
	}

	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public SQLBuilder getSQLBuilder(Action action) {
		return new SQLBuilder(action);
	}

	@Override
	public void destroy() {}

	@Override
	public boolean addRelationship(Column primaryColumn, Column foreignColumn, AssociationType associationType) {
		boolean result = graph.addRelationship(primaryColumn, foreignColumn, associationType);
		boolean reverseResult = false;
		// Reverse relationship
		// Do not add an inverse relationship in the same table
		if (primaryColumn.getTable() != foreignColumn.getTable()) {
			reverseResult = graph.addRelationship(foreignColumn, primaryColumn, associationType.reverse());
		}
		return result || reverseResult;
	}

	@Override
	public boolean deleteRelationship(Column primaryColumn, Column foreignColumn) {
		boolean result = graph.deleteRelationship(primaryColumn, foreignColumn);
		// Delete reverse relationship
		boolean reverseResult = graph.deleteRelationship(foreignColumn, primaryColumn);
		return result || reverseResult;
	}

	@Override
	public boolean deleteRelationship(Table primaryTable, Table foreignTable) {
		return graph.deleteRelationship(primaryTable, foreignTable);
	}

	@Override
	public void cleanRelationshipCache() {
		graph.cleanRelationship();
	}

	@Override
	public Set<Relationship> getAllRelationships() {
		return graph.getAllRelationships();
	}

	@Override
	public Set<RelationshipPath> getRelationships(String primaryTable, String foreignTable) {
		return getRelationships(getTable(primaryTable), getTable(foreignTable));
	}

	@Override
	public Set<RelationshipPath> getRelationships(Table primaryTable, Table foreignTable) {
		return graph.getRelationships(primaryTable, foreignTable);
	}

	@Override
	public AssociationType getAssociationType(Table primaryTable, Table foreignTable) {
		Set<RelationshipPath> relationships = getRelationships(primaryTable, foreignTable);
		Relationship first = Iterables.getFirst(relationships, null).getFirst();
		AssociationType associationType = first.getAssociationType();
		if (associationType == AssociationType.ONE_TO_MANY || associationType == AssociationType.MANY_TO_MANY) {
			return associationType;
		}
		if (relationships.size() > 1) {
			Relationship last = Iterables.getLast(relationships, null).getFirst();
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

	@Override
	public void refresh() {
		initMetadata();
	}

	@Override
	public void refreshTable(String name) {
		Table table = schema.getTableByName(name);
		if (table != null) {
			schema.removeTable(table);
		}
		loadTable(name);
	}

}

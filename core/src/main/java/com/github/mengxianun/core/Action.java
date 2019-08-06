package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.List;

import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.item.FilterItem;
import com.github.mengxianun.core.item.GroupItem;
import com.github.mengxianun.core.item.JoinItem;
import com.github.mengxianun.core.item.LimitItem;
import com.github.mengxianun.core.item.OrderItem;
import com.github.mengxianun.core.item.TableItem;
import com.github.mengxianun.core.item.ValueItem;
import com.github.mengxianun.core.request.Operation;
import com.github.mengxianun.core.request.Template;
import com.github.mengxianun.core.schema.Table;
import com.google.gson.JsonObject;

public class Action {

	private DataContext dataContext;
	private JsonObject requestData;
	private Operation operation;
	private List<TableItem> tableItems;
	private List<ColumnItem> columnItems;
	private List<JoinItem> joinItems;
	private List<FilterItem> filterItems;
	private List<GroupItem> groupItems;
	private List<OrderItem> orderItems;
	private LimitItem limitItem;
	private List<ValueItem> valueItems;
	private ResultType resultType;
	private Template template;
	private String nativeContent;
	private SQLBuilder sqlBuilder;
	private boolean distinct;

	private List<Table> tables;
	private List<Table> joinTables;
	// 是否处理 Join Limit 的情况
	private boolean handleJoinLimit;

	public Action() {
		this.tableItems = new ArrayList<>();
		this.columnItems = new ArrayList<>();
		this.joinItems = new ArrayList<>();
		this.filterItems = new ArrayList<>();
		this.groupItems = new ArrayList<>();
		this.orderItems = new ArrayList<>();
		this.valueItems = new ArrayList<>();
		handleJoinLimit = true;
	}

	public Action(DataContext dataContext) {
		this(dataContext, null);
	}

	public Action(Operation operation) {
		this(null, operation);

	}

	public Action(DataContext dataContext, Operation operation) {
		this();
		this.dataContext = dataContext;
		this.operation = operation;
	}

	public void addTableItem(TableItem tableItem) {
		if (tableItem == null) {
			return;
		}
		this.tableItems.add(tableItem);
	}

	public void addTableItems(List<TableItem> tableItems) {
		if (tableItems == null || tableItems.isEmpty()) {
			return;
		}
		this.tableItems.addAll(tableItems);
	}

	public void addColumnItem(ColumnItem columnItem) {
		if (columnItem == null) {
			return;
		}
		for (ColumnItem existColumnItem : columnItems) {
			if (existColumnItem.getColumn() == columnItem.getColumn() && (existColumnItem.getAlias() != null
					&& existColumnItem.getAlias().equals(columnItem.getAlias()))
					&& (existColumnItem.getExpression() != null
							&& existColumnItem.getExpression().equals(columnItem.getExpression()))) {
				return;
			}
		}
		this.columnItems.add(columnItem);
	}

	public void addColumnItems(List<ColumnItem> columnItems) {
		if (columnItems == null || columnItems.isEmpty()) {
			return;
		}
		columnItems.forEach(this::addColumnItem);
	}

	public void addJoinItem(JoinItem joinItem) {
		if (joinItem == null) {
			return;
		}
		this.joinItems.add(joinItem);
	}

	public void addJoinItems(List<JoinItem> joinItems) {
		if (joinItems == null || joinItems.isEmpty()) {
			return;
		}
		this.joinItems.addAll(joinItems);
	}

	public void addFilterItem(FilterItem filterItem) {
		if (filterItem == null) {
			return;
		}
		this.filterItems.add(filterItem);
	}

	public void addFilterItem(List<FilterItem> filterItems) {
		if (filterItems == null || filterItems.isEmpty()) {
			return;
		}
		this.filterItems.addAll(filterItems);
	}

	public void addGroupItem(GroupItem groupItem) {
		if (groupItem == null) {
			return;
		}
		this.groupItems.add(groupItem);
	}

	public void addGroupItems(List<GroupItem> groupItems) {
		if (groupItems == null || groupItems.isEmpty()) {
			return;
		}
		this.groupItems.addAll(groupItems);
	}

	public void addOrderItem(OrderItem orderItem) {
		if (orderItem == null) {
			return;
		}
		this.orderItems.add(orderItem);
	}

	public void addOrderItems(List<OrderItem> orderItems) {
		if (orderItems == null || orderItems.isEmpty()) {
			return;
		}
		this.orderItems.addAll(orderItems);
	}

	public void addLimitItem(LimitItem limitItem) {
		if (limitItem == null) {
			return;
		}
		this.limitItem = limitItem;
	}

	public void addValueItem(ValueItem valueItem) {
		if (valueItem == null) {
			return;
		}
		this.valueItems.add(valueItem);
	}

	public void addValueItems(List<ValueItem> valueItems) {
		if (valueItems == null || valueItems.isEmpty()) {
			return;
		}
		this.valueItems.addAll(valueItems);
	}

	public boolean isDetail() {
		return operation != null && operation == Operation.DETAIL;
	}

	public boolean isSelect() {
		return operation != null && (operation == Operation.SELECT);
	}

	public boolean isQuery() {
		return isDetail() || isSelect();
	}

	public boolean isUpdate() {
		return operation != null && operation == Operation.UPDATE;
	}

	public boolean isInsert() {
		return operation != null && operation == Operation.INSERT;
	}

	public boolean isDelete() {
		return operation != null && operation == Operation.DELETE;
	}

	public boolean isCRUD() {
		return isQuery() || isInsert() || isUpdate() || isDelete();
	}

	public boolean isTransaction() {
		return operation != null && operation == Operation.TRANSACTION;
	}

	public boolean isStruct() {
		return operation != null && operation == Operation.STRUCT;
	}

	public boolean isStructs() {
		return operation != null && operation == Operation.STRUCTS;
	}

	public boolean isNative() {
		return operation != null && operation == Operation.NATIVE;
	}

	public boolean isResultFile() {
		return resultType != null;
	}

	public boolean isTemplate() {
		return template != null;
	}

	public boolean isJoin() {
		return !joinItems.isEmpty();
	}

	public boolean isLimit() {
		return limitItem != null;
	}

	public boolean columnAliasEnabled() {
		return App.Context.columnAliasEnabled();
	}

	public Action count() {
		Action count = new Action(dataContext, Operation.DETAIL);
		count.build();
		String countSql = sqlBuilder.countSql();
		List<Object> countParams = sqlBuilder.countParams();
		SQLBuilder countSqlBuilder = count.getSqlBuilder();
		countSqlBuilder.setSql(countSql);
		countSqlBuilder.setParams(countParams);
		return count;
	}

	public void build() {
		if (sqlBuilder == null) {
			sqlBuilder = dataContext.getSQLBuilder(this);
		}
		sqlBuilder.toSql();
	}

	public String getSql() {
		return sqlBuilder.getSql();
	}

	public List<Object> getParams() {
		return sqlBuilder.getParams();
	}

	public JsonObject getRequestData() {
		return requestData;
	}

	public DataContext getDataContext() {
		return dataContext;
	}

	public void setDataContext(DataContext dataContext) {
		this.dataContext = dataContext;
	}

	public void setRequestData(JsonObject requestData) {
		this.requestData = requestData;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public List<TableItem> getTableItems() {
		return tableItems;
	}

	public void setTableItems(List<TableItem> tableItems) {
		this.tableItems = tableItems;
	}

	public List<ColumnItem> getColumnItems() {
		return columnItems;
	}

	public void setColumnItems(List<ColumnItem> columnItems) {
		this.columnItems = columnItems;
	}

	public List<JoinItem> getJoinItems() {
		return joinItems;
	}

	public void setJoinItems(List<JoinItem> joinItems) {
		this.joinItems = joinItems;
	}

	public List<FilterItem> getFilterItems() {
		return filterItems;
	}

	public void setFilterItems(List<FilterItem> filterItems) {
		this.filterItems = filterItems;
	}

	public List<GroupItem> getGroupItems() {
		return groupItems;
	}

	public void setGroupItems(List<GroupItem> groupItems) {
		this.groupItems = groupItems;
	}

	public List<OrderItem> getOrderItems() {
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems) {
		this.orderItems = orderItems;
	}

	public LimitItem getLimitItem() {
		return limitItem;
	}

	public void setLimitItem(LimitItem limitItem) {
		this.limitItem = limitItem;
	}

	public List<ValueItem> getValueItems() {
		return valueItems;
	}

	public void setValueItems(List<ValueItem> valueItems) {
		this.valueItems = valueItems;
	}

	public ResultType getResultType() {
		return resultType;
	}

	public void setResultType(ResultType resultType) {
		this.resultType = resultType;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public String getNativeContent() {
		return nativeContent;
	}

	public void setNativeContent(String nativeContent) {
		this.nativeContent = nativeContent;
	}

	public SQLBuilder getSqlBuilder() {
		return sqlBuilder;
	}

	public void setSqlBuilder(SQLBuilder sqlBuilder) {
		this.sqlBuilder = sqlBuilder;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void setTables(List<Table> tables) {
		this.tables = tables;
	}

	public List<Table> getJoinTables() {
		return joinTables;
	}

	public void setJoinTables(List<Table> joinTables) {
		this.joinTables = joinTables;
	}

	public boolean isHandleJoinLimit() {
		return handleJoinLimit;
	}

	public void setHandleJoinLimit(boolean handleJoinLimit) {
		this.handleJoinLimit = handleJoinLimit;
	}

}

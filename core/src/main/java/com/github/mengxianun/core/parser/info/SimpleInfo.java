package com.github.mengxianun.core.parser.info;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.github.mengxianun.core.parser.info.extension.StatementConditionInfo;
import com.github.mengxianun.core.parser.info.extension.StatementValueConditionInfo;
import com.github.mengxianun.core.request.Operation;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SimpleInfo {

	public abstract Operation operation();

	@Nullable
	public abstract SourceInfo source();

	@Nullable
	public abstract TableInfo table();

	public abstract List<ColumnInfo> columns();

	public abstract List<JoinInfo> joins();

	@Nullable
	public abstract WhereInfo where();

	public abstract List<GroupInfo> groups();

	public abstract List<OrderInfo> orders();

	@Nullable
	public abstract LimitInfo limit();

	public abstract List<ValuesInfo> insertValues();

	@Nullable
	public abstract ValuesInfo updateValues();

	@Nullable
	public abstract SqlInfo sql();

	@Nullable
	public abstract NativeInfo nativeInfo();

	@Nullable
	public abstract FileInfo file();

	public abstract List<StatementConditionInfo> statementConditions();

	public abstract List<StatementValueConditionInfo> statementValueConditions();

	public abstract List<SimpleInfo> simples();

	public static Builder builder() {
		return new AutoValue_SimpleInfo.Builder().columns(Collections.emptyList()).joins(Collections.emptyList())
				.where(WhereInfo.create(Collections.emptyList()))
				.groups(Collections.emptyList()).orders(Collections.emptyList())
				.insertValues(Collections.emptyList()).statementConditions(Collections.emptyList())
				.statementValueConditions(Collections.emptyList())
				.simples(Collections.emptyList());
	}

	abstract Builder toBuilder();

	public SimpleInfo withWhere(WhereInfo where) {
		return toBuilder().where(where).build();
	}

	public SimpleInfo withStatementConditions(List<StatementConditionInfo> statementConditions) {
		return toBuilder().statementConditions(statementConditions).build();
	}

	public SimpleInfo withStatementValueConditions(List<StatementValueConditionInfo> statementValueConditions) {
		return toBuilder().statementValueConditions(statementValueConditions).build();
	}

	@AutoValue.Builder
	public abstract static class Builder {

		public abstract Builder operation(Operation operation);

		public abstract Builder source(SourceInfo source);

		public abstract Builder table(TableInfo table);

		public abstract Builder columns(List<ColumnInfo> columns);

		public abstract Builder joins(List<JoinInfo> joins);

		public abstract Builder where(WhereInfo where);

		public abstract Builder groups(List<GroupInfo> groups);

		public abstract Builder orders(List<OrderInfo> orders);

		public abstract Builder limit(LimitInfo limit);

		public abstract Builder insertValues(List<ValuesInfo> values);

		public abstract Builder updateValues(ValuesInfo value);

		public abstract Builder sql(SqlInfo sql);

		public abstract Builder nativeInfo(NativeInfo nativeInfo);

		public abstract Builder file(FileInfo file);

		public abstract Builder statementConditions(List<StatementConditionInfo> statementConditions);

		public abstract Builder statementValueConditions(List<StatementValueConditionInfo> statementValueConditions);

		public abstract Builder simples(List<SimpleInfo> simples);

		public abstract SimpleInfo build();
	}

}

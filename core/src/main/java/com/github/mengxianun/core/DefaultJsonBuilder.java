package com.github.mengxianun.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 预留
 * 
 * @author mengxiangyun
 *
 */
public class DefaultJsonBuilder implements JsonBuilder {

	private String initJson;
	private List<String> tableJsons;
	private List<String> fieldsJsons;
	private List<String> joinJsons;
	private List<String> whereJsons;
	private List<String> groupJsons;
	private List<String> orderJsons;
	private List<String> limitJsons;

	public DefaultJsonBuilder() {
		tableJsons = new ArrayList<>();
		fieldsJsons = new ArrayList<>();
		joinJsons = new ArrayList<>();
		whereJsons = new ArrayList<>();
		groupJsons = new ArrayList<>();
		orderJsons = new ArrayList<>();
		limitJsons = new ArrayList<>();
	}

	public DefaultJsonBuilder(String initJson) {
		this();
		this.initJson = initJson;
	}

	@Override
	public JsonBuilder detail(String tableJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder select(String tableJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder insert(String tableJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder update(String tableJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder delete(String tableJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder fields(String fieldsJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder join(String joinJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder where(String whereJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder group(String groupJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder order(String orderJson) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonBuilder limit(String limitJson) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInitJson() {
		return initJson;
	}

	public void setInitJson(String initJson) {
		this.initJson = initJson;
	}

	public List<String> getTableJsons() {
		return tableJsons;
	}

	public void setTableJsons(List<String> tableJsons) {
		this.tableJsons = tableJsons;
	}

	public List<String> getFieldsJsons() {
		return fieldsJsons;
	}

	public void setFieldsJsons(List<String> fieldsJsons) {
		this.fieldsJsons = fieldsJsons;
	}

	public List<String> getJoinJsons() {
		return joinJsons;
	}

	public void setJoinJsons(List<String> joinJsons) {
		this.joinJsons = joinJsons;
	}

	public List<String> getWhereJsons() {
		return whereJsons;
	}

	public void setWhereJsons(List<String> whereJsons) {
		this.whereJsons = whereJsons;
	}

	public List<String> getGroupJsons() {
		return groupJsons;
	}

	public void setGroupJsons(List<String> groupJsons) {
		this.groupJsons = groupJsons;
	}

	public List<String> getOrderJsons() {
		return orderJsons;
	}

	public void setOrderJsons(List<String> orderJsons) {
		this.orderJsons = orderJsons;
	}

	public List<String> getLimitJsons() {
		return limitJsons;
	}

	public void setLimitJsons(List<String> limitJsons) {
		this.limitJsons = limitJsons;
	}

}

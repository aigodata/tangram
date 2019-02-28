package com.github.mengxianun.core;

/**
 * Json 构建器, 预留
 * 
 * @author mengxiangyun
 *
 */
public interface JsonBuilder {

	public JsonBuilder detail(String tableJson);

	public JsonBuilder select(String tableJson);

	public JsonBuilder insert(String tableJson);

	public JsonBuilder update(String tableJson);

	public JsonBuilder delete(String tableJson);

	public JsonBuilder fields(String fieldsJson);

	public JsonBuilder join(String joinJson);

	public JsonBuilder where(String whereJson);

	public JsonBuilder group(String groupJson);

	public JsonBuilder order(String orderJson);

	public JsonBuilder limit(String limitJson);

}

package com.github.mengxianun.core;

import com.google.gson.JsonElement;

public interface DataResultSet {

	public long getTook();

	public Object getData();

	public JsonElement getJsonData();

	public int getCode();

	public String getMessage();

	public boolean isFile();

	public boolean succeed();

}

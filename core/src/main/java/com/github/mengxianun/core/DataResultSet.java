package com.github.mengxianun.core;

import com.google.gson.JsonElement;

public interface DataResultSet {

	public int getCode();

	public String getMessage();

	public Object getData();

	public JsonElement getJsonData();

	public boolean isFile();

	public boolean succeed();

}

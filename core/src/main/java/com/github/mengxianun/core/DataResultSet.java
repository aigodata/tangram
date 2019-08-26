package com.github.mengxianun.core;

import java.io.ByteArrayOutputStream;

import com.google.gson.JsonElement;

public interface DataResultSet {

	public int getCode();

	public String getMessage();

	public Object getData();

	public JsonElement getJsonData();

	public boolean isFile();

	public String getFilename();

	public ByteArrayOutputStream getOutputStream();

	public boolean succeed();

}

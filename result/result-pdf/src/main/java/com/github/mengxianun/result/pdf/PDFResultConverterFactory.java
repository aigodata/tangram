package com.github.mengxianun.result.pdf;

import com.github.mengxianun.core.ResultConverter;
import com.github.mengxianun.core.ResultConverterFactory;
import com.github.mengxianun.core.utils.Type;
import com.google.gson.JsonElement;

import java.util.Map;

public  class PDFResultConverterFactory implements ResultConverterFactory {

	@Override
	public String getType() {
		return Type.ConverterType.PDF.type();
	}

	@Override
	public ResultConverter create(Map<String, Object> properties, Map<String, Object> header, JsonElement data) {
		return PDFResultConverter.getInstance(properties, header, data);
	}
}

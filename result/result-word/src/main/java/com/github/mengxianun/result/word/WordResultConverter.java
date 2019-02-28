package com.github.mengxianun.result.word;

import com.github.mengxianun.core.AbstractResultConverter;
import com.google.gson.JsonElement;

import java.io.*;
import java.util.Map;

public class WordResultConverter extends AbstractResultConverter {

	private WordResultConverter() {
	}
	private WordResultConverter(Map<String, Object> properties, Map<String, Object> header, JsonElement data) {
		super.dataInit(properties, header, data);
		this.initStyle();
	}
	public static WordResultConverter getInstance(Map<String, Object> properties, Map<String, Object> header, JsonElement data) {
		return new WordResultConverter(properties, header, data);
	}

	private static final String TEMPLATE_FILENAME = "wod_template.html"; // 模板文件名
	private static final String TR = "<tr style='mso-yfti-irow:0;mso-yfti-firstrow:yes;mso-yfti-lastrow:yes'>${tr}</tr>";
	private static final String TD = "<td width=553 valign=top style='width:415.0pt;border:solid windowtext 1.0pt;mso-border-alt:solid windowtext .5pt;padding:0cm 5.4pt 0cm 5.4pt'>" +
		"<p class=MsoNormal><span style='font-family:\"微软雅黑\",sans-serif'/tr>${td}</span></p></td>";

	@Override
	protected InputStream export() throws Exception {

		InputStream inputStream = this.getClass().getResourceAsStream(TEMPLATE_FILENAME);

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

		StringBuffer html = new StringBuffer();
		String str;
		while ((str = reader.readLine()) != null) {
			html.append(str);
		}
		String templateContent = html.toString();

		StringBuilder tableContent = new StringBuilder();


		if (!data.isEmpty()) {
			Map<String, Object> map = data.get(0);
			StringBuilder tds = new StringBuilder();
			for (String key : map.keySet()) {
				tds.append(replaceParams(TD, "td", getDisplayName(key)));
			}
			tableContent.append(replaceParams(TR, "tr", tds.toString()));
		}
		System.out.println("headers--->"+tableContent.toString());

		for (Map<String, Object> map : data) {
			StringBuilder tds = new StringBuilder();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				tds.append(replaceParams(TD, "td", entry.getValue().toString()));
			}
			tableContent.append(replaceParams(TR, "tr", tds.toString()));
		}
		templateContent = replaceParams(templateContent, "tableContent", tableContent.toString());
		System.out.println(templateContent);

		return create(templateContent);
	}

	public InputStream create(String html) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();//构建字节输出流
		// 追加写入日志
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos, "UTF-8"));
		bw.write(html);
		bw.flush();
		bw.close();

		return new ByteArrayInputStream(baos.toByteArray());
	}
	private void initStyle() {

	}
}

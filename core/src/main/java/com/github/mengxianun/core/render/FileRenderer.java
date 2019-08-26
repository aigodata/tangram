package com.github.mengxianun.core.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;

import com.github.mengxianun.core.Action;
import com.github.mengxianun.core.config.TableConfig;
import com.github.mengxianun.core.data.Row;
import com.github.mengxianun.core.exception.DataException;
import com.github.mengxianun.core.item.ColumnItem;
import com.github.mengxianun.core.request.FileType;
import com.github.mengxianun.core.schema.Column;
import com.github.mengxianun.core.schema.ColumnType;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class FileRenderer extends AbstractRenderer<OutputStream> {

	public FileRenderer(Action action) {
		super(action);
	}

	@Override
	public ByteArrayOutputStream render(List<Row> rows) {
		ByteArrayOutputStream outputStream;
		FileType fileType = action.getFileType();
		Objects.requireNonNull(fileType, "Unrecognized file type");
		switch (fileType) {
		case XLS:
		case XLSX:
			outputStream = renderExcel(rows);
			break;

		default:
			throw new UnsupportedOperationException("Unsupported file format");
		}
		return outputStream;
	}

	private ByteArrayOutputStream renderExcel(List<Row> rows) {
		List<ColumnItem> columnItems = action.getColumnItems();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (HSSFWorkbook wb = new HSSFWorkbook()) {
			HSSFSheet sheet = wb.createSheet();

			// 头部信息
			HSSFRow header = sheet.createRow(0);
			for (int i = 0; i < columnItems.size(); i++) {
				String key = columnItems.get(i).getKey();

				HSSFCell cell = header.createCell(i);
				cell.setCellType(CellType.STRING);
				cell.setCellValue(key);
			}

			// 数据
			for (int i = 0; i < rows.size(); i++) {
				Row row = rows.get(i);
				HSSFRow hssfRow = sheet.createRow(i + 1);
				for (int j = 0; j < columnItems.size(); j++) {
					HSSFCell cell = hssfRow.createCell(j);
					cell.setCellType(CellType.STRING);

					Object value = row.getValue(j);
					if (value == null) {
						cell.setCellValue((String) null);
						continue;
					}
					Column column = columnItems.get(j).getColumn();
					if (column == null) {
						if (value instanceof Number) {
							cell.setCellValue(Double.valueOf(value.toString()));
						} else if (value instanceof Boolean) {
							cell.setCellValue(Boolean.valueOf(value.toString()));
						} else {
							cell.setCellValue(value.toString());
						}
					} else {
						JsonObject config = column.getConfig();
						if (config.has(TableConfig.COLUMN_IGNORE)
								&& config.get(TableConfig.COLUMN_IGNORE).getAsBoolean()) { // 列忽略
							continue;
						}
						ColumnType columnType = column.getType();
						if (columnType.isNumber()) {
							Number number = null;
							if (value instanceof JsonPrimitive) {
								number = ((JsonPrimitive) value).getAsNumber();
							} else {
								number = (Number) value;
							}
							cell.setCellValue(Double.valueOf(number.toString()));
						} else if (columnType.isBoolean()) {
							cell.setCellValue(Boolean.valueOf(value.toString()));
						} else if (columnType.isLiteral()) {
							cell.setCellValue(value.toString());
						} else if (columnType.isJson() || columnType.isArray()) {
							cell.setCellValue(value.toString());
						} else {
							cell.setCellValue(value.toString());
						}
					}
				}
			}
			wb.write(byteArrayOutputStream);
		} catch (IOException e) {
			throw new DataException("Excel build failed", e);
		}

		return byteArrayOutputStream;
	}

}

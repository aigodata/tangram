package com.github.mengxianun.core.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
			outputStream = renderXls(rows);
			break;
		case XLSX:
			outputStream = renderXlsx(rows);
			break;

		default:
			throw new UnsupportedOperationException("Unsupported file type");
		}
		return outputStream;
	}

	private ByteArrayOutputStream renderXls(List<Row> rows) {
		return renderExcel(rows, new HSSFWorkbook());
	}

	private ByteArrayOutputStream renderXlsx(List<Row> rows) {
		return renderExcel(rows, new XSSFWorkbook());
	}

	private ByteArrayOutputStream renderExcel(List<Row> rows, Workbook workbook) {
		List<ColumnItem> columnItems = action.getColumnItems();
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try (Workbook wb = workbook) {
			String tableDisplayName = action.getPrimaryTable().getDisplayName();
			Sheet sheet = wb.createSheet(tableDisplayName);

			// Header
			org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
			for (int i = 0; i < columnItems.size(); i++) {
				String key = getColumnKey(columnItems.get(i));

				Cell cell = header.createCell(i);
				cell.setCellValue(key);
			}

			// Data
			for (int i = 0; i < rows.size(); i++) {
				Row dataRow = rows.get(i);
				org.apache.poi.ss.usermodel.Row row = sheet.createRow(i + 1);
				for (int j = 0; j < columnItems.size(); j++) {
					Cell cell = row.createCell(j);

					Object value = dataRow.getValue(j);
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
						} else if (columnType.isArray()) {
							cell.setCellValue(Arrays.toString((Object[]) value));
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

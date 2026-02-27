package io.jonasg.mother.csv;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class CsvMother {

	private String[] headers;
	private final List<Row> rows = new ArrayList<>();
	private final List<String[]> pendingRows = new ArrayList<>();

	public static CsvMother of(String filePath) {
		return new CsvMother(filePath);
	}

	public CsvMother(String filePath) {
		parseCsv(filePath);
	}

	public CsvMother withRow(String line) {
		pendingRows.add(line.split(","));
		return this;
	}

	public CsvMother withRow(Consumer<LineBuilder> columnBuilderConsumer) {
		var lineBuilder = new LineBuilder();
		columnBuilderConsumer.accept(lineBuilder);
		String[] values = lineBuilder.build().split(",", -1);
		pendingRows.add(values);
		return this;
	}

	public CsvMother withRowColumnValue(Integer rowIndex, String column, Object value) {
		if (rowIndex >= 0 && rowIndex < rows.size()) {
			Row row = rows.get(rowIndex);
			setColumnValue(row, column, value);
		}
		return this;
	}

	public CsvMother withRowColumnValue(Predicate<Row> predicate, String column, Object value) {
		for (Row row : rows) {
			if (predicate.test(row)) {
				setColumnValue(row, column, value);
				break;
			}
		}
		return this;
	}

	private void setColumnValue(Row row, String columnName, Object value) {
		String[] rowHeaders = row.getHeaders();
		String[] rowValues = row.getValues();
		for (int i = 0; i < rowHeaders.length; i++) {
			if (rowHeaders[i].equals(columnName)) {
				rowValues[i] = String.valueOf(value);
				break;
			}
		}
	}

	public String build() {
		StringBuilder sb = new StringBuilder();

		sb.append(String.join(",", headers));

		for (Row row : rows) {
			sb.append("\n").append(String.join(",", row.getValues()));
		}

		for (int i = 0; i < pendingRows.size(); i++) {
			sb.append("\n").append(String.join(",", pendingRows.get(i)));
		}

		return sb.toString();
	}

	private void parseCsv(String filePath) {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
			if (is == null) {
				throw new RuntimeException("Unable to open file " + filePath);
			}
			List<String[]> allLines = new CSVReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))
					.readAll();

			if (!allLines.isEmpty()) {
				headers = allLines.get(0);
				for (int i = 1; i < allLines.size(); i++) {
					rows.add(new Row(headers, allLines.get(i)));
				}
			}
		} catch (IOException | CsvException e) {
			throw new RuntimeException("Error parsing CSV", e);
		}
	}
}

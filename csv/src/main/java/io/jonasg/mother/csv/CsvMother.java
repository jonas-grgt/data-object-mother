package io.jonasg.mother.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A utility class for building CSV content as a string based on an existing CSV
 * file.
 * It allows for modifying the values of existing rows and adding new rows.
 */
public class CsvMother {

	private final List<Row> rows = new ArrayList<>();
	private final List<String[]> pendingRows = new ArrayList<>();
	private String[] headers;

	protected CsvMother(String filePath) {
		parseCsv(filePath);
	}

	/**
	 * Creates a new CsvMother instance by loading a CSV file from the classpath.
	 *
	 * @param filePath
	 *            the path to the CSV file in the classpath (e.g.,
	 *            "data/sample.csv")
	 * @return a new CsvMother instance initialized with the content of the
	 *         specified CSV file
	 */
	public static CsvMother of(String filePath) {
		return new CsvMother(filePath);
	}

	/**
	 * Adds a new row to the CSV content based on a comma-separated string.
	 * The values in the string will be split by commas and added as a new row.
	 *
	 * @param line
	 *            a comma-separated string representing the values of the new row
	 *            (e.g., "value1,value2,value3")
	 * @return the current CsvMother instance for method chaining
	 */
	public CsvMother withRow(String line) {
		pendingRows.add(line.split(","));
		return this;
	}

	/**
	 * Adds a new row to the CSV content using a LineBuilder, which allows for
	 * building the row values in a more structured way.
	 *
	 * @param columnBuilderConsumer
	 *            a Consumer that accepts a LineBuilder to build the values of the
	 *            new row.
	 *            The LineBuilder provides methods to add values for each column.
	 * @return the current CsvMother instance for method chaining
	 */
	public CsvMother withRow(Consumer<LineBuilder> columnBuilderConsumer) {
		var lineBuilder = new LineBuilder();
		columnBuilderConsumer.accept(lineBuilder);
		String[] values = lineBuilder.build().split(",", -1);
		pendingRows.add(values);
		return this;
	}

	/**
	 * Modifies the value of a specific column in an existing row identified by its
	 * index.
	 *
	 * @param rowIndex
	 *            the index of the row to modify (0-based)
	 * @param column
	 *            the name of the column to modify
	 * @param value
	 *            the new value to set for the specified column
	 * @return the current CsvMother instance for method chaining
	 * @throws IllegalArgumentException
	 *             if the specified column is not found in the CSV headers
	 * @throws IndexOutOfBoundsException
	 *             if the row index is out of bounds
	 */
	public CsvMother withRowColumnValue(Integer rowIndex, String column, Object value) {
		if (rowIndex < 0 || rowIndex >= rows.size()) {
			throw new IndexOutOfBoundsException(
					"Row index " + rowIndex + " is out of bounds. Valid range: 0-" + (rows.size() - 1));
		}
		Row row = rows.get(rowIndex);
		setColumnValue(row, column, value);
		return this;
	}

	/**
	 * Modifies the value of a specific column in the first row that matches the
	 * given predicate.
	 *
	 * @param predicate
	 *            a Predicate that tests each row to find a match
	 * @param column
	 *            the name of the column to modify
	 * @param value
	 *            the new value to set for the specified column
	 * @return the current CsvMother instance for method chaining
	 * @throws IllegalArgumentException
	 *             if no row matches the predicate or if the specified column is not
	 *             found
	 */
	public CsvMother withRowColumnValue(Predicate<Row> predicate, String column, Object value) {
		Row matchingRow = rows.stream()
				.filter(predicate)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("No row found matching the given predicate"));
		setColumnValue(matchingRow, column, value);
		return this;
	}

	/**
	 * Builds the final CSV content as a string, including the headers and all rows
	 * (both modified existing rows and newly added rows).
	 * 
	 * @return a string representation of the customized CSV content
	 */
	public String build() {
		var sb = new StringBuilder();

		sb.append(String.join(",", headers));

		rows.forEach(r -> sb.append("\n").append(String.join(",", r.values())));

		pendingRows.forEach(r -> sb.append("\n").append(String.join(",", r)));

		return sb.toString();
	}

	private void setColumnValue(Row row, String columnName, Object value) {
		String[] rowHeaders = row.headers();
		String[] rowValues = row.values();
		for (int i = 0; i < rowHeaders.length; i++) {
			if (rowHeaders[i].equals(columnName)) {
				rowValues[i] = String.valueOf(value);
				return;
			}
		}
		throw new IllegalArgumentException(
				"Column '" + columnName + "' not found. Available columns: " + String.join(", ", rowHeaders));
	}

	private void parseCsv(String filePath) {
		try (InputStream is = this.getClass().getClassLoader().getResourceAsStream(filePath)) {
			if (is == null) {
				throw new RuntimeException("Unable to open file " + filePath);
			}
			var allLines = new CSVReader(new java.io.InputStreamReader(is, StandardCharsets.UTF_8))
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

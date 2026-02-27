package io.jonasg.mother.csv;

public class Row {
	private final String[] headers;
	private final String[] values;

	public Row(String[] headers, String[] values) {
		this.headers = headers;
		this.values = values;
	}

	public String column(String headerName) {
		for (int i = 0; i < headers.length; i++) {
			if (headers[i].equals(headerName)) {
				return values[i];
			}
		}
		return null;
	}

	public String[] getValues() {
		return values;
	}

	public String[] getHeaders() {
		return headers;
	}
}

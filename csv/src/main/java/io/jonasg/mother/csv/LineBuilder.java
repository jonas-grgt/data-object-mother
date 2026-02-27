package io.jonasg.mother.csv;

import java.util.ArrayList;
import java.util.List;

public class LineBuilder {
	private final List<String> columns = new ArrayList<>();

	public LineBuilder withColumn(String value) {
		columns.add(value);
		return this;
	}

	public String build() {
		return String.join(",", columns);
	}
}

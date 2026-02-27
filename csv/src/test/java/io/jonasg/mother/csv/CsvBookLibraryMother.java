package io.jonasg.mother.csv;

public class CsvBookLibraryMother {
	public static Builder library() {
		return new Builder("mother-data/books.csv");
	}

	public static class Builder extends AbstractCsvObjectMotherBuilder<Builder> {

		public Builder(String filePath) {
			super(filePath);
		}
	}

}

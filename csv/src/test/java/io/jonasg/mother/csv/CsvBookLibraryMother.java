package io.jonasg.mother.csv;

public class CsvBookLibraryMother extends CsvMother {

	public static CsvBookLibraryMother library() {
		return new CsvBookLibraryMother("mother-data/books.csv");
	}

	public CsvBookLibraryMother(String filePath) {
		super(filePath);
	}

	public CsvBookLibraryMother withAuthor(String author) {
		return (CsvBookLibraryMother) withRowColumnValue(0, "author", author);
	}

	public CsvBookLibraryMother withTitle(String title) {
		return (CsvBookLibraryMother) withRowColumnValue(0, "title", title);
	}
}

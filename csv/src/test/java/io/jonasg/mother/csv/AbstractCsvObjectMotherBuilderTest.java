package io.jonasg.mother.csv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AbstractCsvObjectMotherBuilderTest {

	@Test
	void buildWithoutModification() {
		// given
		var libraryBuilder = CsvMother.of("mother-data/books.csv");

		// when
		String actual = libraryBuilder.build();

		// then
		assertThat(actual).isEqualTo("""
				author,title,genre
				Leo Tolstoy,War and Peace,Historical Novel
				Fyodor Dostoevsky,Crime and Punishment,Psychological Novel
				Alexander Pushkin,Eugene Onegin,Novel in Verse
				Nikolai Gogol,Dead Souls,Satirical Novel""");
	}

	@Test
	void withCustomStringBasedRow() {
		// given
		var libraryBuilder = CsvMother.of("mother-data/books.csv");

		// when
		String actual = libraryBuilder
				.withRow("Ivan Turgenev,Fathers and Sons,Philosophical Novel")
				.build();

		// then
		assertThat(actual).isEqualTo("""
				author,title,genre
				Leo Tolstoy,War and Peace,Historical Novel
				Fyodor Dostoevsky,Crime and Punishment,Psychological Novel
				Alexander Pushkin,Eugene Onegin,Novel in Verse
				Nikolai Gogol,Dead Souls,Satirical Novel
				Ivan Turgenev,Fathers and Sons,Philosophical Novel""");
	}

	@Test
	void withAdjustedColumnValueForRowByIndex() {
		// given
		var libraryBuilder = CsvMother.of("mother-data/books.csv");

		// when
		String actual = libraryBuilder
				.withRowColumnValue(2, "title", "The Prophet")
				.build();

		// then
		assertThat(actual).isEqualTo("""
				author,title,genre
				Leo Tolstoy,War and Peace,Historical Novel
				Fyodor Dostoevsky,Crime and Punishment,Psychological Novel
				Alexander Pushkin,The Prophet,Novel in Verse
				Nikolai Gogol,Dead Souls,Satirical Novel""");
	}

	@Test
	void withAdjustedColumnValueForRowByFilter() {
		// given
		var libraryBuilder = CsvMother.of("mother-data/books.csv");

		// when
		String actual = libraryBuilder
				.withRowColumnValue(
						r -> "Eugene Onegin".equals(r.column("title")),
						"title",
						"The Prophet")
				.build();

		// then
		assertThat(actual).isEqualTo("""
				author,title,genre
				Leo Tolstoy,War and Peace,Historical Novel
				Fyodor Dostoevsky,Crime and Punishment,Psychological Novel
				Alexander Pushkin,The Prophet,Novel in Verse
				Nikolai Gogol,Dead Souls,Satirical Novel""");
	}
}
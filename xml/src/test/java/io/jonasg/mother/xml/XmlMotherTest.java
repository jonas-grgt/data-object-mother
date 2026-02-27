package io.jonasg.mother.xml;

import org.junit.jupiter.api.Test;
import org.xmlunit.assertj.XmlAssert;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XmlMotherTest {

	@Test
	void buildWithoutModification() throws Exception {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder.build();

		// then
		try (InputStream resourceAsStream = getClass().getResourceAsStream("/mother-data/book.xml")) {
			var expected = new String(resourceAsStream.readAllBytes());
			XmlAssert.assertThat(actual).and(expected).normalizeWhitespace().areSimilar();
		}
	}

	@Test
	void withElementUpdatesValue() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withElement("//title", "New Title")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//title").isEqualTo("New Title");
	}

	@Test
	void withNestedElementUpdatesValue() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withElement("//author/name", "Ernest Hemingway")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//author/name").isEqualTo("Ernest Hemingway");
	}

	@Test
	void withElementOnIndexedElement() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withElement("//genres/genre[1]/type", "classic")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//genres/genre[1]/type").isEqualTo("classic");
	}

	@Test
	void withAttributeUpdatesValueOnRoot() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withAttribute("", "id", "123")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//book/@id").isEqualTo("123");
	}

	@Test
	void withRemovedElement() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withRemovedElement("//title")
				.build();

		// then
		XmlAssert.assertThat(actual).doesNotHaveXPath("//title");
	}

	@Test
	void withRemovedNestedElement() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withRemovedElement("//author/name")
				.build();

		// then
		XmlAssert.assertThat(actual).doesNotHaveXPath("//author/name");
	}

	@Test
	void withRemovedIndexedElement() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withRemovedElement("//genres/genre[1]")
				.build();

		// then - genre element should be gone
		XmlAssert.assertThat(actual).doesNotHaveXPath("//genres/genre[1]/type[text()='novel']");
	}

	@Test
	void withAttributeThrowsWhenElementNotFound() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when & then
		assertThatThrownBy(() -> builder
				.withAttribute("//nonexistent", "attr", "value"))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void withAttributeOnChildElement() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withAttribute("//author", "type", "novelist")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//author/@type").isEqualTo("novelist");
	}

	@Test
	void withElementSetsAttributeOnRoot() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withElement("//@id", "999")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//book/@id").isEqualTo("999");
	}

	@Test
	void withElementSetsAttributeOnChild() {
		// given
		var builder = XmlMother.of("mother-data/book.xml");

		// when
		String actual = builder
				.withElement("//author/@type", "novelist")
				.build();

		// then
		XmlAssert.assertThat(actual).valueByXPath("//author/@type").isEqualTo("novelist");
	}
}

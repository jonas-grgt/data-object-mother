package io.jonasg.mother.json;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.jonasg.mother.json.JsonMother;

class AbstractJsonObjectMotherBuilderTest {

	@Test
	void overrideExistingProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("id", 123)
				.build();

		// then
		assertEquals("""
				{
				  "id": 123,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void overrideExistingNestedProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("author.name", "Ernest Hemingway")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "Ernest Hemingway"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void removeExistingProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withRemovedProperty("title")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void removeExistingNestedProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withRemovedProperty("author.name")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": { },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void removeElementFromArray() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withRemovedProperty("genres[1]")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("language", "English")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "language": "English",
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewNestedProperty() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("translations.english", true)
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "translations": {
					"english": true
				  },
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addMultipleNewNestedPropertiesAtTheSameLevel() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("translations.english", true)
				.withProperty("translations.french", false)
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "translations": {
					"english": true,
					"french": false
				  },
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewObjectToArrayElement() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("genres[2].type", "drama")
				.build();

		// then
		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    },
				    {
				      "type": "drama"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewObjectToNewArrayElement() throws JSONException {
		// given
		var bookBuilder = JsonMother.of("mother-data/book.json");

		// when
		String actual = bookBuilder
				.withProperty("translations[0].language", "French")
				.withProperty("translations[1].language", "Spanish")
				.build();

		// then
		assertEquals("""
				{
				       "id": 1,
				       "title": "The Great Gatsby",
				       "author": {
				         "name": "F. Scott Fitzgerald"
				       },
				       "published": {
				         "year": 1925
				       },
				       "genres": [
				         {
				           "type": "novel"
				         },
				         {
				           "type": "fiction"
				         }
				       ],
				       "translations": [
				         {
				           "language": "French"
				         },
				         {
				           "language": "Spanish"
				         }
				       ]
				}
				""", actual, true);
	}

	@Test
	void addNewObjectToMultiDigitArrayIndex() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("genres[10].type", "thriller")
				.build();

		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {
				      "type": "novel"
				    },
				    {
				      "type": "fiction"
				    },
				    null,
				    null,
				    null,
				    null,
				    null,
				    null,
				    null,
				    null,
				    {
				      "type": "thriller"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void removeNestedPropertyInArrayElement() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withRemovedProperty("genres[0].type")
				.build();

		assertEquals("""
				{
				  "id": 1,
				  "title": "The Great Gatsby",
				  "author": {
				    "name": "F. Scott Fitzgerald"
				  },
				  "published": {
				    "year": 1925
				  },
				  "genres": [
				    {},
				    {
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}
}

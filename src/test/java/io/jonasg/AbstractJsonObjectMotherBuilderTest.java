package io.jonasg;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import org.json.JSONException;
import org.junit.jupiter.api.Test;

import io.jonasg.BookMother.Builder;

class AbstractJsonObjectMotherBuilderTest {

	@Test
	void overrideExistingProperty() throws JSONException {
		// given
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

		// when
		String actual = bookBuilder
				.withRemovedProperty("genres[0]")
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
				      "type": "fiction"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewProperty() throws JSONException {
		// given
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

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
	void addMultipleNewNestedPropertiesAtSomeLevel() throws JSONException {
		// given
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

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
		Builder bookBuilder = BookMother.book();

		// when
		String actual = bookBuilder
				.withProperty("translations[0].language", "French")
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
				         }
				       ]
				}
				""", actual, true);
	}
}

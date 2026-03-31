package io.jonasg.mother.json;

import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class JsonMotherTest {

	@Test
	void overrideExistingProperty() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/id", 123)
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/author/name", "Ernest Hemingway")
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withRemovedProperty("/title")
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withRemovedProperty("/author/name")
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withRemovedProperty("/genres/1")
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
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewProperty() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/language", "English")
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/translations/english", true)
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/translations/english", true)
				.withProperty("/translations/french", false)
				.build();

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
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/genres/2/type", "drama")
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
				    {
				      "type": "drama"
				    }
				  ]
				}
				""", actual, true);
	}

	@Test
	void addNewObjectToNewArrayElement() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/translations/0/language", "French")
				.withProperty("/translations/1/language", "Spanish")
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
				.withProperty("/genres/10/type", "thriller")
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
				.withRemovedProperty("/genres/0/type")
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

	@Test
	void addMapObject() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/prop", Map.of("key1", "value1", "key2", 2))
				.build();

		assertEquals("""
				{
				  "id" : 1,
				  "title" : "The Great Gatsby",
				  "author" : {
				    "name" : "F. Scott Fitzgerald"
				  },
				  "published" : {
				    "year" : 1925
				  },
				  "genres" : [ {
				    "type" : "novel"
				  }, {
				    "type" : "fiction"
				  } ],
				  "prop" : {
				    "key1" : "value1",
				    "key2" : 2
				  }
				}
				""", actual, true);
	}

	@Test
	void addMapJavaObject() throws JSONException {
		var bookBuilder = JsonMother.of("mother-data/book.json");

		String actual = bookBuilder
				.withProperty("/prop", new TestData("value1", 2))
				.build();

		assertEquals("""
				{
				  "id" : 1,
				  "title" : "The Great Gatsby",
				  "author" : {
				    "name" : "F. Scott Fitzgerald"
				  },
				  "published" : {
				    "year" : 1925
				  },
				  "genres" : [ {
				    "type" : "novel"
				  }, {
				    "type" : "fiction"
				  } ],
				  "prop" : {
				    "name" : "value1",
				    "value" : 2
				  }
				}
				""", actual, true);
	}

	@Test
	void loadFromClassRelativePath() throws JSONException {
		var builder = JsonMother.of(JsonMotherTest.class, "package-book.json");

		String actual = builder
				.withProperty("/id", 999)
				.build();

		assertEquals("""
				{
				  "id": 999,
				  "title": "Package Test Book"
				}
				""", actual, true);
	}

	@Test
	void throwsWhenFilePathIsGiven() {
		var message = assertThrows(IllegalArgumentException.class,
				() -> JsonMother.of(JsonMotherTest.class, "file/path.json")).getMessage();

		Assertions.assertEquals(
				"When Loading file relative to class, the file name must not contain path separators: file/path.json",
				message);
	}

	@Test
	void throwsWhenFileNotFoundInPackage() {
		assertThrows(IllegalArgumentException.class, () -> JsonMother.of(JsonMotherTest.class, "nonexistent.json"));
	}

	@SuppressWarnings("unused")
	public static class TestData {
		private String name;
		private int value;

		public TestData(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}
	}
}

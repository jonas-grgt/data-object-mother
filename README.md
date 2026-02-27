# Data Mothers

## json-object-mother 📄

The `AbstractJsonObjectMotherBuilder` class provides a flexible mechanism for
manipulating JSON objects during testing. This library facilitates the creation,
modification, and validation of JSON structures, making it easier to test JSON-dependent
components in your Java applications. 

### Example usage 📝

#### Define a JSON data mother

```java
public class JsonBookMother {

    public static Builder book() {
        return new Builder("mother-data/book.json");
    }

    public static class Builder extends AbstractJsonObjectMotherBuilder<Builder> {

        public Builder(String filePath) {
            super(filePath);
        }
    }
}
```

#### Use the JSON data mother

```java
// Load JSON from file and modify properties
String json = JsonBookMother.book()
    .withProperty("title", "New Title")
    .withProperty("author.name", "Jane Doe")
    .withProperty("tags[0]", "fiction")
    .withRemovedProperty("deprecatedField")
    .build();
```

#### Available methods 🔧

- `withProperty(String path, Object value)` - Set a property using dot notation
  - Nested properties: `"author.name"`
  - Array elements: `"tags[0]"`, `"items[2].id"`
- `withRemovedProperty(String path)` - Remove a property
- `build()` - Returns the modified JSON as a string

---

## csv-object-mother 📊

The `AbstractCsvObjectMotherBuilder` class provides functionality for manipulating CSV data
during testing. It allows loading CSV files, adding new rows, and modifying existing
column values.

### Example usage 📝

#### Define a CSV data mother

```java
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
```

#### Use the CSV data mother

```java
// Load CSV from file
String csv = CsvBookLibraryMother.library()
    .build();

// Add a new row (string-based)
String csvWithRow = CsvBookLibraryMother.library()
    .withRow("Author Name,Book Title,Genre")
    .build();

// Add a new row (builder-based)
String csvWithRow = CsvBookLibraryMother.library()
    .withRow(builder -> builder
        .withColumn("Author Name")
        .withColumn("Book Title")
        .withColumn("Genre"))
    .build();

// Modify column value by row index (0-based)
String csvModified = CsvBookLibraryMother.library()
    .withRowColumnValue(0, "title", "Modified Title")
    .build();

// Modify column value by predicate (first matching row)
String csvModified = CsvBookLibraryMother.library()
    .withRowColumnValue(
        row -> "War and Peace".equals(row.column("title")),
        "author",
        "Leo Tolstoy (Updated)")
    .build();

// Combine operations
String csv = CsvBookLibraryMother.library()
    .withRowColumnValue(0, "title", "New Title")
    .withRow("New Author,New Book,New Genre")
    .build();
```

#### Available methods 🔧

- `withRow(String line)` - Add a new row using a comma-separated string
- `withRow(Consumer<LineBuilder> columnBuilder)` - Add a new row using a builder pattern
- `withRowColumnValue(Integer rowIndex, String column, Object value)` - Modify a column value by 0-based row index
- `withRowColumnValue(Predicate<Row> predicate, String column, Object value)` - Modify first row matching predicate
- `build()` - Returns the modified CSV as a string

#### Row predicate usage 🔍

The predicate-based method allows filtering rows:

```java
// Find row where title equals "War and Peace"
.withRowColumnValue(
    row -> "War and Peace".equals(row.column("title")),
    "author",
    "Updated Author")

// Find row where genre contains "Novel"    
.withRowColumnValue(
    row -> row.column("genre").contains("Novel"),
    "genre",
    "Classic Novel")
```

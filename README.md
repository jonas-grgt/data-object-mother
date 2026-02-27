# Data Mothers

## json-object-mother 📄

The `JsonMother` class provides a flexible mechanism for
manipulating JSON objects during testing. This library facilitates the creation,
modification, and validation of JSON structures, making it easier to test JSON-dependent
components in your Java applications. 

### Quick start 📝

```java
// Load JSON from file and modify properties
String json = JsonMother.of("book.json")
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

#### Extending for custom DSL 📦

If you need a custom DSL, extend `JsonMother`:

```java
public class BookMother extends JsonMother {

    public static BookMother book() {
        return new BookMother("mother-data/book.json");
    }

    public BookMother(String filePath) {
        super(filePath);
    }

    // Custom DSL methods
    public BookMother withTitle(String title) {
        return (BookMother) withProperty("title", title);
    }

    public BookMother withAuthorName(String name) {
        return (BookMother) withProperty("author.name", name);
    }
}

// Usage
String json = BookMother.book()
    .withTitle("New Title")           // Custom DSL
    .withProperty("tags", "fiction")  // Still available
    .build();
```

---

## csv-object-mother 📊

The `CsvMother` class provides functionality for manipulating CSV data
during testing. It allows loading CSV files, adding new rows, and modifying existing
column values.

### Quick start 📝

```java
// Load CSV from file
String csv = CsvMother.of("books.csv")
    .build();

// Add a new row (string-based)
String csvWithRow = CsvMother.from("books.csv")
    .withRow("Author Name,Book Title,Genre")
    .build();

// Add a new row (builder-based)
String csvWithRow = CsvMother.from("books.csv")
    .withRow(builder -> builder
        .withColumn("Author Name")
        .withColumn("Book Title")
        .withColumn("Genre"))
    .build();

// Modify column value by row index (0-based)
String csvModified = CsvMother.from("books.csv")
    .withRowColumnValue(0, "title", "Modified Title")
    .build();

// Modify column value by predicate (first matching row)
String csvModified = CsvMother.from("books.csv")
    .withRowColumnValue(
        row -> "War and Peace".equals(row.column("title")),
        "author",
        "Leo Tolstoy (Updated)")
    .build();

// Combine operations
String csv = CsvMother.of("books.csv")
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

#### Extending for custom DSL 📦

If you need a custom DSL, extend `CsvMother`:

```java
public class BookLibraryMother extends CsvMother {

    public static BookLibraryMother library() {
        return new BookLibraryMother("mother-data/books.csv");
    }

    public BookLibraryMother(String filePath) {
        super(filePath);
    }

    // Custom DSL methods
    public BookLibraryMother withFirstAuthor(String author) {
        return (BookLibraryMother) withRowColumnValue(0, "author", author);
    }

    public BookLibraryMother withFirstTitle(String title) {
        return (BookLibraryMother) withRowColumnValue(0, "title", title);
    }
}

// Usage
String csv = BookLibraryMother.library()
    .withFirstTitle("New Title")           // Custom DSL
    .withRow("New Author,New Book,Genre")  // Still available
    .build();
```

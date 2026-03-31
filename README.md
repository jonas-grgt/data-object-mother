# Data Mothers

## json-object-mother 📄

The `JsonMother` class provides a flexible mechanism for
manipulating JSON objects during testing. This library facilitates the creation,
modification, and validation of JSON structures, making it easier to test JSON-dependent
components in your Java applications. 

### Quick start 📝

#### Installation

```xml
<dependency>
    <groupId>io.jonasg</groupId>
    <artifactId>json-object-mother</artifactId>
    <version>${object-mother.version}</version>
    <scope>test</scope>
</dependency>
```

#### Usage

Load JSON from file and modify properties using JSON Pointer (RFC 6901)
```java
String json = JsonMother.of("book.json")
    .withProperty("/title", "New Title")
    .withProperty("/author/name", "Jane Doe")
    .withProperty("/tags/0", "fiction")
    .withRemovedProperty("/deprecatedField")
    .build();
```

Load JSON relative from given class.
If class is in package com.example, it will look for resource at /com/example/book.json
```java
String json = JsonMother.of(getClass(), "book.json")
    .withProperty("/title", "New Title")
    .withProperty("/author/name", "Jane Doe")
    .withProperty("/tags/0", "fiction")
    .withRemovedProperty("/deprecatedField")
    .build();
```

#### Available methods 🔧

- `withProperty(String jsonPointer, Object value)` - Set a property using
  <a href="https://tools.ietf.org/html/rfc6901">RFC 6901 JSON Pointer</a> notation
  - `jsonPointer`:
    - Root: `"/"`
    - Root value: `"/"` (when used with a primitive value)
    - Nested properties: `"/author/name"`
    - Array elements: `"/tags/0"`, `"/items/2/id"`
  - `value`:
    - Primitive types: `String`, `Number`, `Boolean`
    - Complex types: `Map`, `List`, custom objects (converted to JSON)
- `withRemovedProperty(String jsonPointer)` - Remove a property using JSON Pointer
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
        return (BookMother) withProperty("/title", title);
    }

    public BookMother withAuthorName(String name) {
        return (BookMother) withProperty("/author/name", name);
    }
}

// Usage
String json = BookMother.book()
    .withTitle("New Title")           // Custom DSL
    .withProperty("/tags/0", "fiction")  // Still available
    .build();
```

---

## csv-object-mother 📊

#### Installation

```xml
<dependency>
    <groupId>io.jonasg</groupId>
    <artifactId>csv-object-mother</artifactId>
    <version>${object-mother.version}</version>
    <scope>test</scope>
</dependency>
```

The `CsvMother` class provides functionality for manipulating CSV data
during testing. It allows loading CSV files, adding new rows, and modifying existing
column values.

### Quick start 📝

```java
// Load CSV from file
String csv = CsvMother.of("books.csv")
    .build();

// Add a new row (string-based)
String csvWithRow = CsvMother.of("books.csv")
    .withRow("Author Name,Book Title,Genre")
    .build();

// Add a new row (builder-based)
String csvWithRow = CsvMother.of("books.csv")
    .withRow(builder -> builder
        .withColumn("Author Name")
        .withColumn("Book Title")
        .withColumn("Genre"))
    .build();

// Modify column value by row index (0-based)
String csvModified = CsvMother.of("books.csv")
    .withRowColumnValue(0, "title", "Modified Title")
    .build();

// Modify column value by predicate (first matching row)
String csvModified = CsvMother.of("books.csv")
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

// Load CSV with custom delimiter (semicolon)
String csv = CsvMother.of("books.csv", ';')
    .build();

// Load CSV relative from given class.
// If class is in package com.example, it will look for resource at /com/example/books.csv
String csv = CsvMother.of(getClass(), "books.csv")
    .build();

// Load CSV relative to class with custom delimiter
String csv = CsvMother.of(getClass(), "books.csv", ';')
    .build();
```

#### Available methods 🔧

- `of(String filePath)` - Load CSV from file path
- `of(String filePath, char delimiter)` - Load CSV with custom delimiter
- `of(Class<?> clazz, String fileName)` - Load CSV relative to class's package
- `of(Class<?> clazz, String fileName, char delimiter)` - Load CSV relative to class's package with custom delimiter
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

---

## xml-object-mother 📝

#### Installation

```xml
<dependency>
    <groupId>io.jonasg</groupId>
    <artifactId>xml-object-mother</artifactId>
    <version>${object-mother.version}</version>
    <scope>test</scope>
</dependency>
```

The `XmlMother` class provides functionality for manipulating XML data
during testing. It allows loading XML files, modifying elements and attributes,
and removing elements using XPath expressions.

### Quick start 📝

```java
// Load XML from file
String xml = XmlMother.of("book.xml")
    .build();

// Modify element text using XPath
String xmlModified = XmlMother.of("book.xml")
    .withElement("//title", "New Title")
    .build();

// Modify nested element
String xmlModified = XmlMother.of("book.xml")
    .withElement("//author/name", "Jane Doe")
    .build();

// Modify indexed element (XPath is 1-based)
String xmlModified = XmlMother.of("book.xml")
    .withElement("//genres/genre[1]/type", "classic")
    .build();

// Set attribute on root element
String xmlModified = XmlMother.of("book.xml")
    .withAttribute("", "id", "123")
    .build();

// Set attribute using XPath
String xmlModified = XmlMother.of("book.xml")
    .withElement("//@id", "456")
    .build();

// Set attribute on child element
String xmlModified = XmlMother.of("book.xml")
    .withElement("//author/@type", "novelist")
    .build();

// Remove element
String xmlModified = XmlMother.of("book.xml")
    .withRemovedElement("//title")
    .build();

// Remove nested element
String xmlModified = XmlMother.of("book.xml")
    .withRemovedElement("//author/name")
    .build();
```

#### Available methods 🔧

- `withElement(String xpath, Object value)` - Set element text or attribute using XPath
  - Element: `"//title"`, `"//author/name"`
  - Indexed (1-based): `"//genres/genre[1]/type"`
  - Attribute on root: `"//@id"`
  - Attribute on child: `"//author/@type"`
- `withAttribute(String xpath, String attrName, Object value)` - Set attribute explicitly
  - Empty path for root element: `""`
  - XPath for element: `"//author"`
- `withRemovedElement(String xpath)` - Remove element by XPath
- `build()` - Returns the modified XML as a string

#### XPath examples 📍

| XPath | Description |
|-------|-------------|
| `//title` | Select title element |
| `//author/name` | Select name element under author |
| `//genres/genre[1]` | Select first genre element (1-based) |
| `//@id` | Select id attribute on root |
| `//author/@type` | Select type attribute on author element |

#### Extending for custom DSL 📦

If you need a custom DSL, extend `XmlMother`:

```java
public class BookXmlMother extends XmlMother {

    public static BookXmlMother book() {
        return new BookXmlMother("mother-data/book.xml");
    }

    public BookXmlMother(String filePath) {
        super(filePath);
    }

    // Custom DSL methods
    public BookXmlMother withTitle(String title) {
        return (BookXmlMother) withElement("//title", title);
    }

    public BookXmlMother withAuthorName(String name) {
        return (BookXmlMother) withElement("//author/name", name);
    }
}

// Usage
String xml = BookXmlMother.book()
    .withTitle("New Title")           // Custom DSL
    .withElement("//genres/genre[1]/type", "classic")  // Still available
    .build();
```

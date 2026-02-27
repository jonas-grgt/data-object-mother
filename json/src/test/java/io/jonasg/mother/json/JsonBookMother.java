package io.jonasg.mother.json;

public class JsonBookMother extends JsonMother {

	public static JsonBookMother book() {
		return new JsonBookMother("mother-data/book.json");
	}

	public JsonBookMother(String filePath) {
		super(filePath);
	}

	public JsonBookMother withTitle(String title) {
		return (JsonBookMother) withProperty("title", title);
	}

	public JsonBookMother withAuthorName(String name) {
		return (JsonBookMother) withProperty("author.name", name);
	}
}

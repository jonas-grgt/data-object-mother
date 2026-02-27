package io.jonasg.mother.json;

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

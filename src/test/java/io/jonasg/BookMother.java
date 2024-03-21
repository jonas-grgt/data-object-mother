package io.jonasg;

public class BookMother {
	public static Builder book() {
		return new Builder("mother-data/book.json");
	}

	public static class Builder extends AbstractJsonObjectMotherBuilder<Builder> {

		public Builder(String filePath) {
			super(filePath);
		}
	}
}

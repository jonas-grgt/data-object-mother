package io.jonasg.mother.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class JsonMother {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private final ObjectNode rootNode;

	public static JsonMother of(String filePath) {
		return new JsonMother(filePath);
	}

	public JsonMother(String filePath) {
		try (var reader = readerForFile(filePath)) {
			JsonNode tempRoot = objectMapper.readTree(reader);
			if (tempRoot.isObject()) {
				rootNode = (ObjectNode) tempRoot;
			} else {
				throw new IllegalArgumentException("Only JSON objects as content are supported.");
			}
		} catch (IOException e) {
			throw new RuntimeException("Error processing JSON", e);
		}
	}

	public JsonMother withProperty(@Nullable String field, @Nullable Object value) {
		if (field == null || field.isEmpty()) {
			throw new IllegalArgumentException("Property path cannot be null or empty");
		}

		String[] parts = field.split("\\.");
		JsonNode current = rootNode;

		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			if (isArray(part)) {
				int bracketIdx = part.indexOf('[');
				String arrayName = part.substring(0, bracketIdx);
				int index = Integer.parseInt(part.substring(bracketIdx + 1, part.length() - 1));

				JsonNode arrayNode = current.path(arrayName);
				if (arrayNode.isMissingNode() || !arrayNode.isArray()) {
					arrayNode = ((ObjectNode) current).putArray(arrayName);
				}

				ArrayNode array = (ArrayNode) arrayNode;
				while (array.size() <= index) {
					array.addNull();
				}

				if (array.get(index).isNull() || !array.get(index).isObject()) {
					array.set(index, new ObjectNode(JsonNodeFactory.instance));
				}
				current = array.get(index);
			} else {
				JsonNode next = current.path(part);
				if (next.isMissingNode() || !next.isObject()) {
					next = ((ObjectNode) current).putObject(part);
				}
				current = next;
			}
		}

		String key = parts[parts.length - 1];
		if (current.isObject()) {
			((ObjectNode) current).putPOJO(key, value);
		} else {
			throw new IllegalArgumentException("Cannot set property on non-object: " + field);
		}

		return this;
	}

	public JsonMother withRemovedProperty(@Nullable String property) {
		if (property == null || property.isEmpty()) {
			throw new IllegalArgumentException("Property path cannot be null or empty");
		}

		String[] parts = property.split("\\.");
		JsonNode current = rootNode;

		for (int i = 0; i < parts.length - 1; i++) {
			current = current.path(parts[i]);
			if (current.isMissingNode() || !current.isObject()) {
				throw new IllegalArgumentException("Invalid path in JSON: " + property);
			}
		}

		if (current.isObject()) {
			if (isArray(parts[parts.length - 1])) {
				int bracketIdx = parts[parts.length - 1].indexOf('[');
				String arrayName = parts[parts.length - 1].substring(0, bracketIdx);
				int index = Integer.parseInt(parts[parts.length - 1].substring(bracketIdx + 1,
						parts[parts.length - 1].length() - 1));
				ArrayNode array = (ArrayNode) current.path(arrayName);
				if (array.size() > index) {
					array.remove(index);
				}
			} else {
				((ObjectNode) current).remove(parts[parts.length - 1]);
			}
		}

		return this;
	}

	private boolean isArray(String part) {
		int idx = part.indexOf('[');
		return idx > 0
				&& part.length() >= 3
				&& part.charAt(idx + 1) >= '0'
				&& part.charAt(idx + 1) <= '9'
				&& part.charAt(idx + 2) == ']';
	}

	public String build() {
		return rootNode.toString();
	}

	private Reader readerForFile(String filePath) {
		var inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			throw new IllegalArgumentException("Unable to open file: " + filePath);
		}
		return new InputStreamReader(inputStream);
	}
}

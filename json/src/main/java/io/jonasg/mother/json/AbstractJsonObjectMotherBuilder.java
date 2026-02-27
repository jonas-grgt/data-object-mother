package io.jonasg.mother.json;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class AbstractJsonObjectMotherBuilder<T extends AbstractJsonObjectMotherBuilder<T>> {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private final ObjectNode rootNode;

	public AbstractJsonObjectMotherBuilder(String filePath) {
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

	@SuppressWarnings({ "unchecked", "UnusedReturnValue" })
	public T withProperty(String field, Object value) {
		String[] parts = field.split("\\.");
		JsonNode current = rootNode;

		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			if (isArray(part)) {
				String arrayName = part.substring(0, part.indexOf('['));
				int index = Integer.parseInt(part.substring(part.indexOf('[') + 1, part.length() - 1));

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
		}

		return (T) this;
	}

	@SuppressWarnings({ "unchecked", "UnusedReturnValue" })
	public T withRemovedProperty(String property) {
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
				String arrayName = parts[parts.length - 1].substring(0, parts[parts.length - 1].indexOf('['));
				int index = Integer.parseInt(parts[parts.length - 1].substring(parts[parts.length - 1].indexOf('[') + 1,
						parts[parts.length - 1].length() - 1));
				ArrayNode array = (ArrayNode) current.path(arrayName);
				if (array.size() > index) {
					array.remove(index);
				}
			} else {
				((ObjectNode) current).remove(parts[parts.length - 1]);
			}
		}

		return (T) this;
	}

	private boolean isArray(String part) {
		return part.matches(".*\\[\\d+]$");
	}

	public String build() {
		return rootNode.toString();
	}

	private Reader readerForFile(String filePath) {
		var inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			throw new RuntimeException("Unable to open file " + filePath);
		}
		return new InputStreamReader(inputStream);
	}
}

package io.jonasg.mother.json;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * A utility class for building JSON objects - as string - based on an existing
 * JSON file.
 * <p>
 * It allows for modifying properties of the JSON structure using
 * <a href="https://tools.ietf.org/html/rfc6901">RFC 6901 JSON Pointer</a>
 * notation for nested properties and array indexing.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * 
 * <pre>
 * var builder = JsonMother.of("data/book.json");
 * String json = builder
 * 		.withProperty("/author/name", "Ernest Hemingway")
 * 		.withProperty("/genres/0/type", "fiction")
 * 		.withRemovedProperty("/published/year")
 * 		.build();
 * </pre>
 */
public class JsonMother {

	private final ObjectNode root;
	private final ObjectMapper objectMapper;

	private JsonMother(ObjectNode root, ObjectMapper objectMapper) {
		this.root = root;
		this.objectMapper = objectMapper;
	}

	/**
	 * Creates a new JsonMother instance by loading a JSON file from the classpath.
	 *
	 * @param filePath
	 *            the path to the JSON file in the classpath (e.g.,
	 *            "data/sample.json")
	 * @return a new JsonMother instance initialized with the content of the
	 *         specified JSON file
	 * @throws IllegalArgumentException
	 *             if the file cannot be found, or if the content is not a JSON
	 *             object
	 * @throws RuntimeException
	 *             if there is an error processing the JSON content
	 */
	public static JsonMother of(String filePath) {
		var mapper = new ObjectMapper();
		try (var is = JsonMother.class.getClassLoader().getResourceAsStream(filePath)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource not found: " + filePath);
			}
			JsonNode node = mapper.readTree(is);
			return new JsonMother((ObjectNode) node, mapper);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load JSON from: " + filePath, e);
		}
	}

	/**
	 * Modifies the JSON structure by setting a property at the specified JSON
	 * Pointer path to the given value.
	 *
	 * @param jsonPointer
	 *            the path to the property to set, using
	 *            <a href="https://tools.ietf.org/html/rfc6901">RFC 6901 JSON
	 *            Pointer</a>
	 *            notation (e.g., "/author/name" or "/genres/0/type")
	 * @param value
	 *            the value to set at the specified path; can be a primitive type,
	 *            a String, or any object that can be converted to JSON using
	 *            Jackson's ObjectMapper
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withProperty(String jsonPointer, Object value) {
		JsonPointer pointer = JsonPointer.compile(jsonPointer);

		if (pointer.matches()) {
			setRootValue(value);
			return this;
		}

		JsonPointer parentPointer = pointer.head();
		JsonNode parentNode = getOrCreateParentNode(parentPointer, pointer);

		String lastSegment = pointer.last().toString().replace("/", "");

		if (parentNode instanceof ObjectNode objectNode) {
			objectNode.putPOJO(lastSegment, value);
		} else if (parentNode instanceof ArrayNode arrayNode) {
			try {
				int index = Integer.parseInt(lastSegment);
				while (arrayNode.size() <= index) {
					arrayNode.addNull();
				}
				arrayNode.set(index, convertValue(value));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid array index: " + lastSegment);
			}
		}

		return this;
	}

	private JsonNode getOrCreateParentNode(JsonPointer pointer, JsonPointer originalPointer) {
		if (pointer.matches()) {
			return root;
		}

		JsonNode node = root.at(pointer);
		if (node != null && !node.isNull() && !node.isMissingNode()) {
			return node;
		}

		JsonPointer parentPointer = pointer.head();
		JsonNode parentNode = getOrCreateParentNode(parentPointer, originalPointer);

		String segment = pointer.last().toString().replace("/", "");

		boolean isArrayIndex = isNumeric(segment);
		if (!isArrayIndex && !pointer.matches()) {
			String fullPath = originalPointer.toString();
			String[] parts = fullPath.split("/");
			int currentDepth = countParts(pointer.toString());
			if (currentDepth < parts.length - 1) {
				String nextSegment = parts[currentDepth + 1];
				isArrayIndex = isNumeric(nextSegment);
			}
		}

		JsonNode newNode;
		if (parentNode.isArray()) {
			newNode = objectMapper.createObjectNode();
		} else {
			newNode = isArrayIndex ? objectMapper.createArrayNode() : objectMapper.createObjectNode();
		}

		if (parentNode instanceof ObjectNode objectNode) {
			objectNode.set(segment, newNode);
		} else if (parentNode instanceof ArrayNode arrayNode) {
			try {
				int index = Integer.parseInt(segment);
				while (arrayNode.size() <= index) {
					arrayNode.addNull();
				}
				arrayNode.set(index, newNode);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid array index: " + segment);
			}
		}

		return newNode;
	}

	private int countParts(String path) {
		if (path.isEmpty() || path.equals("/")) {
			return 0;
		}
		return path.split("/").length - 1;
	}

	/**
	 * Removes a property from the JSON structure at the specified JSON Pointer
	 * path.
	 *
	 * @param jsonPointer
	 *            the path to the property to remove, using
	 *            <a href="https://tools.ietf.org/html/rfc6901">RFC 6901 JSON
	 *            Pointer</a>
	 *            notation (e.g., "/author/name" or "/genres/0/type")
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withRemovedProperty(String jsonPointer) {
		JsonPointer pointer = JsonPointer.compile(jsonPointer);

		JsonPointer parentPointer = pointer.head();
		JsonNode parentNode;

		if (parentPointer.matches()) {
			parentNode = root;
		} else {
			parentNode = root.at(parentPointer);
		}

		if (parentNode == null) {
			return this;
		}

		String lastSegment = pointer.last().toString().replace("/", "");

		if (parentNode instanceof ObjectNode objectNode) {
			objectNode.remove(lastSegment);
		} else if (parentNode instanceof ArrayNode arrayNode) {
			try {
				int index = Integer.parseInt(lastSegment);
				if (index >= 0 && index < arrayNode.size()) {
					arrayNode.remove(index);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid array index: " + lastSegment);
			}
		}

		return this;
	}

	/**
	 * Builds the final JSON string representation of the modified JSON structure.
	 * 
	 * @return a pretty-printed JSON string representing the current state of the
	 *         JSON structure
	 */
	public String build() {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to serialize JSON", e);
		}
	}

	private void setRootValue(Object value) {
		if (value instanceof JsonNode jsonNode) {
			if (jsonNode.isObject()) {
				root.removeAll();
				root.setAll((ObjectNode) jsonNode);
			}
		} else {
			root.putPOJO("_value", value);
		}
	}

	private boolean isNumeric(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	private JsonNode convertValue(Object value) {
		if (value == null) {
			return objectMapper.nullNode();
		} else if (value instanceof JsonNode jsonNode) {
			return jsonNode;
		}
		return objectMapper.valueToTree(value);
	}
}

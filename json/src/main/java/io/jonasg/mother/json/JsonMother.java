package io.jonasg.mother.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for building JSON objects - as string - based on an existing
 * file.
 * It allows for modifying properties of the JSON structure using dot notation
 * for nested properties and array indexing.
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
	 * Modifies the JSON structure by setting a property at the specified path to
	 * the given value.
	 *
	 * @param path
	 *            the path to the property to set, using dot notation for nested
	 *            properties and array indexing (e.g., "author.name" or
	 *            "genres[0].type")
	 * @param value
	 *            the value to set at the specified path; can be a primitive type,
	 *            a String, or any object that can be converted to JSON using
	 *            Jackson's ObjectMapper
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withProperty(String path, Object value) {
		List<PathSegment> segments = parsePath(path);
		JsonNode current = root;

		for (int i = 0; i < segments.size() - 1; i++) {
			PathSegment segment = segments.get(i);
			boolean nextIsArrayIndex = isNextSegmentArrayIndex(segments, i);
			current = navigateOrCreate(current, segment, nextIsArrayIndex);
		}

		PathSegment lastSegment = segments.get(segments.size() - 1);
		setValue(current, lastSegment, value);

		return this;
	}

	/**
	 * Removes a property from the JSON structure at the specified path.
	 *
	 * @param path
	 *            the path to the property to remove, using dot notation for nested
	 *            properties and array indexing (e.g., "author.name" or
	 *            "genres[0].type")
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withRemovedProperty(String path) {
		List<PathSegment> segments = parsePath(path);
		JsonNode current = root;

		for (int i = 0; i < segments.size() - 1; i++) {
			PathSegment segment = segments.get(i);
			current = navigate(current, segment);
			if (current == null) {
				return this;
			}
		}

		PathSegment lastSegment = segments.get(segments.size() - 1);
		if (current instanceof ObjectNode objectNode) {
			if (lastSegment.propertyName != null) {
				objectNode.remove(lastSegment.propertyName);
			}
		} else if (current instanceof ArrayNode arrayNode) {
			if (lastSegment.arrayIndex != null) {
				int idx = lastSegment.arrayIndex;
				if (idx >= 0 && idx < arrayNode.size()) {
					arrayNode.remove(idx);
				}
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

	private List<PathSegment> parsePath(String path) {
		List<PathSegment> segments = new ArrayList<>();
		StringBuilder currentToken = new StringBuilder();

		for (int i = 0; i < path.length(); i++) {
			char c = path.charAt(i);

			if (c == '.') {
				if (!currentToken.isEmpty()) {
					segments.add(new PathSegment(currentToken.toString(), null));
					currentToken.setLength(0);
				}
			} else if (c == '[') {
				if (!currentToken.isEmpty()) {
					segments.add(new PathSegment(currentToken.toString(), null));
					currentToken.setLength(0);
				}
				int closeBracket = path.indexOf(']', i);
				if (closeBracket == -1) {
					throw new IllegalArgumentException("Unclosed bracket in path: " + path);
				}
				String indexStr = path.substring(i + 1, closeBracket);
				int index = Integer.parseInt(indexStr);
				segments.add(new PathSegment(null, index));
				i = closeBracket;
			} else {
				currentToken.append(c);
			}
		}

		if (!currentToken.isEmpty()) {
			segments.add(new PathSegment(currentToken.toString(), null));
		}

		return segments;
	}

	private JsonNode navigateOrCreate(JsonNode node, PathSegment segment, boolean nextIsArrayIndex) {
		if (segment.propertyName != null) {
			if (node.has(segment.propertyName)) {
				return node.get(segment.propertyName);
			} else {
				JsonNode newNode;
				if (nextIsArrayIndex) {
					newNode = objectMapper.createArrayNode();
				} else {
					newNode = objectMapper.createObjectNode();
				}
				((ObjectNode) node).set(segment.propertyName, newNode);
				return newNode;
			}
		} else if (segment.arrayIndex != null) {
			ArrayNode arrayNode;
			if (node.isArray()) {
				arrayNode = (ArrayNode) node;
			} else if (node.isObject()) {
				ObjectNode objectNode = (ObjectNode) node;
				String arrayKey = "_arr_" + segment.arrayIndex;
				JsonNode existing = objectNode.get(arrayKey);
				if (existing != null && existing.isArray()) {
					arrayNode = (ArrayNode) existing;
				} else {
					arrayNode = objectMapper.createArrayNode();
					objectNode.set(arrayKey, arrayNode);
				}
			} else {
				return null;
			}

			while (arrayNode.size() <= segment.arrayIndex) {
				arrayNode.addNull();
			}

			JsonNode element = arrayNode.get(segment.arrayIndex);
			if (element == null || element.isNull()) {
				ObjectNode newNode = objectMapper.createObjectNode();
				arrayNode.set(segment.arrayIndex, newNode);
				return newNode;
			}
			return element;
		}
		throw new IllegalArgumentException("Invalid segment");
	}

	private boolean isNextSegmentArrayIndex(List<PathSegment> segments, int currentIndex) {
		if (currentIndex + 1 >= segments.size()) {
			return false;
		}
		return segments.get(currentIndex + 1).arrayIndex != null;
	}

	private JsonNode navigate(JsonNode node, PathSegment segment) {
		if (segment.propertyName != null) {
			return node.get(segment.propertyName);
		} else if (segment.arrayIndex != null) {
			if (node.isArray()) {
				ArrayNode arrayNode = (ArrayNode) node;
				int idx = segment.arrayIndex;
				if (idx >= 0 && idx < arrayNode.size()) {
					return arrayNode.get(idx);
				}
			}
			return null;
		}
		return null;
	}

	private void setValue(JsonNode node, PathSegment segment, Object value) {
		if (segment.propertyName != null) {
			((ObjectNode) node).putPOJO(segment.propertyName, value);
		} else if (segment.arrayIndex != null) {
			ArrayNode arrayNode;
			if (node.isArray()) {
				arrayNode = (ArrayNode) node;
			} else if (node.isObject()) {
				ObjectNode objectNode = (ObjectNode) node;
				String arrayKey = "_arr_" + segment.arrayIndex;
				JsonNode existing = objectNode.get(arrayKey);
				if (existing != null && existing.isArray()) {
					arrayNode = (ArrayNode) existing;
				} else {
					arrayNode = objectMapper.createArrayNode();
					objectNode.set(arrayKey, arrayNode);
				}
			} else {
				return;
			}

			while (arrayNode.size() <= segment.arrayIndex) {
				arrayNode.addNull();
			}
			arrayNode.set(segment.arrayIndex(), convertValue(value));
		}
	}

	private JsonNode convertValue(Object value) {
		JsonNodeFactory factory = JsonNodeFactory.instance;
		if (value == null) {
			return factory.nullNode();
		} else if (value instanceof JsonNode) {
			return (JsonNode) value;
		} else if (value instanceof Number) {
			if (value instanceof Integer || value instanceof Long) {
				return factory.numberNode(((Number) value).longValue());
			} else if (value instanceof Double || value instanceof Float) {
				return factory.numberNode(((Number) value).doubleValue());
			}
		} else if (value instanceof Boolean) {
			return factory.booleanNode((Boolean) value);
		} else if (value instanceof String) {
			return factory.textNode((String) value);
		}
		return objectMapper.valueToTree(value);
	}

	private record PathSegment(String propertyName, Integer arrayIndex) {
	}
}

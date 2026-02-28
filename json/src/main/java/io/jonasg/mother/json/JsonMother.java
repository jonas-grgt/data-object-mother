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

/**
 * A utility class for building JSON objects - as string - based on an existing
 * file.
 * It allows for modifying properties of the JSON structure using dot notation
 * for nested properties and array indexing.
 */
public class JsonMother {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private final ObjectNode rootNode;

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
		return new JsonMother(filePath);
	}

	protected JsonMother(String filePath) {
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

	/**
	 * Sets a property in the JSON structure.
	 * <p>
	 * The field parameter supports dot notation for nested properties and array
	 * indexing. For example:
	 * <ul>
	 * <li>"author.name" sets the "name" property of the "author" object.</li>
	 * <li>"genres[0].type" sets the "type" property of the first element in the
	 * "genres" array.</li>
	 * </ul>
	 * 
	 * @param field
	 *            the property path in dot notation, supporting nested properties
	 *            and array indexing
	 * @param value
	 *            the value to set for the specified property
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withProperty(String field, @Nullable Object value) {
		if (field.isEmpty()) {
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

	/**
	 * Completely removes a property from the JSON structure.
	 * 
	 * @param property
	 *            the property path in dot notation, supporting nested properties
	 *            and array indexing (e.g., "author.name" or "genres[0].type")
	 * @return the current JsonMother instance for method chaining
	 */
	public JsonMother withRemovedProperty(String property) {
		if (property.isEmpty()) {
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

	/**
	 * Builds the final JSON string based on the current state of the JSON
	 * structure.
	 * 
	 * @return the JSON string representation of the current state of the
	 *         (customized) JSON structure
	 */
	public String build() {
		return rootNode.toString();
	}

	private boolean isArray(String part) {
		int idx = part.indexOf('[');
		return idx > 0
				&& part.length() >= 3
				&& part.charAt(idx + 1) >= '0'
				&& part.charAt(idx + 1) <= '9'
				&& part.charAt(idx + 2) == ']';
	}

	private Reader readerForFile(String filePath) {
		var inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			throw new IllegalArgumentException("Unable to open file: " + filePath);
		}
		return new InputStreamReader(inputStream);
	}
}

package io.jonasg.mother.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonMother {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private static final Pattern BRACKET_PATTERN = Pattern.compile("(\\w+)\\[(\\d+)\\]");

	private final ObjectNode rootNode;

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

	public JsonMother withProperty(String field, @Nullable Object value) {
		if (field == null || field.isEmpty()) {
			throw new IllegalArgumentException("Property path cannot be null or empty");
		}

		String[] parts = splitPath(field);
		JsonNode current = rootNode;

		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			if (isArrayIndex(part)) {
				String arrayName = arrayName(part);
				int index = arrayIndex(part);

				JsonNode arrayNode = current.path(arrayName);
				if (arrayNode.isMissingNode() || !arrayNode.isArray()) {
					arrayNode = ((ObjectNode) current).putArray(arrayName);
				}

				ArrayNode array = (ArrayNode) arrayNode;
				while (array.size() <= index) {
					array.addNull();
				}

				if (array.get(index).isNull() || !array.get(index).isObject()) {
					array.set(index, objectMapper.createObjectNode());
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
		if (isArrayIndex(key)) {
			String arrayName = arrayName(key);
			int index = arrayIndex(key);

			JsonNode arrayNode = current.path(arrayName);
			if (arrayNode.isMissingNode() || !arrayNode.isArray()) {
				arrayNode = ((ObjectNode) current).putArray(arrayName);
			}

			ArrayNode array = (ArrayNode) arrayNode;
			while (array.size() <= index) {
				array.addNull();
			}
			array.set(index, objectMapper.valueToTree(value));
		} else if (current.isObject()) {
			((ObjectNode) current).putPOJO(key, value);
		} else {
			throw new IllegalArgumentException("Cannot set property on non-object: " + field);
		}

		return this;
	}

	public JsonMother withRemovedProperty(String property) {
		if (property == null || property.isEmpty()) {
			throw new IllegalArgumentException("Property path cannot be null or empty");
		}

		String pointer = toJsonPointer(property);
		String parentPath = parentPath(pointer);
		String key = leafKey(pointer);

		JsonNode parent = parentPath.isEmpty() ? rootNode : rootNode.at(parentPath);
		if (parent.isMissingNode()) {
			throw new IllegalArgumentException("Invalid path in JSON: " + property);
		}

		if (parent.isArray()) {
			int index = Integer.parseInt(key);
			ArrayNode array = (ArrayNode) parent;
			if (array.size() > index) {
				array.remove(index);
			}
		} else if (parent.isObject()) {
			((ObjectNode) parent).remove(key);
		}

		return this;
	}

	public String build() {
		return rootNode.toString();
	}

	private String toJsonPointer(String path) {
		String result = path;
		Matcher matcher = BRACKET_PATTERN.matcher(result);
		var sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "/" + matcher.group(1) + "/" + matcher.group(2));
		}
		matcher.appendTail(sb);
		result = sb.toString();
		if (!result.startsWith("/")) {
			result = "/" + result;
		}
		result = result.replace('.', '/');
		return result;
	}

	private String parentPath(String pointer) {
		int lastSlash = pointer.lastIndexOf('/');
		if (lastSlash <= 0) {
			return "";
		}
		return pointer.substring(0, lastSlash);
	}

	private String[] splitPath(String path) {
		StringBuffer sb = new StringBuffer();
		Matcher matcher = BRACKET_PATTERN.matcher(path);
		while (matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(1) + "___" + matcher.group(2)));
		}
		matcher.appendTail(sb);
		return sb.toString().replace('.', '/').split("/");
	}

	private boolean isArrayIndex(String part) {
		return part.contains("___");
	}

	private String arrayName(String part) {
		return part.substring(0, part.indexOf("___"));
	}

	private int arrayIndex(String part) {
		return Integer.parseInt(part.substring(part.indexOf("___") + 3));
	}

	private String leafKey(String pointer) {
		int lastSlash = pointer.lastIndexOf('/');
		return pointer.substring(lastSlash + 1);
	}

	private Reader readerForFile(String filePath) {
		var inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			throw new IllegalArgumentException("Unable to open file: " + filePath);
		}
		return new InputStreamReader(inputStream);
	}
}

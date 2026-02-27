package io.jonasg.mother.xml;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * A utility class for building XML content as a string based on an existing XML
 * file.
 * It allows for modifying elements and attributes using XPath expressions.
 */
public class XmlMother {

	private final Document document;
	private final Element rootElement;
	private final XPath xpath;

	protected XmlMother(String filePath) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			try (Reader reader = readerForFile(filePath)) {
				document = builder.parse(new org.xml.sax.InputSource(reader));
			}
			rootElement = document.getDocumentElement();
			xpath = XPathFactory.newInstance().newXPath();
		} catch (Exception e) {
			throw new RuntimeException("Error parsing XML", e);
		}
	}

	/**
	 * Creates a new XmlMother instance by loading an XML file from the classpath.
	 *
	 * @param filePath
	 *            the path to the XML file in the classpath (e.g.,
	 *            "data/sample.xml")
	 * @return a new XmlMother instance initialized with the content of the
	 *         specified XML file
	 */
	public static XmlMother of(String filePath) {
		return new XmlMother(filePath);
	}

	/**
	 * Sets the text content of an element or attribute specified by an XPath
	 * expression.
	 * <p>
	 * XPath examples:
	 * <ul>
	 * <li>"//title" - selects title element</li>
	 * <li>"//author/name" - selects name element under author</li>
	 * <li>"//genres/genre[1]/type" - selects type element at index 1 (XPath is
	 * 1-based)</li>
	 * <li>"//@id" - selects id attribute on root element</li>
	 * <li>"//author/@type" - selects type attribute on author element</li>
	 * </ul>
	 *
	 * @param xpath
	 *            the XPath expression to select the element or attribute
	 * @param value
	 *            the text value or attribute value to set
	 * @return the current XmlMother instance for method chaining
	 */
	public XmlMother withElement(String xpath, @Nullable String value) {
		if (xpath == null || xpath.isEmpty()) {
			throw new IllegalArgumentException("XPath expression cannot be null or empty");
		}

		if (xpath.contains("/@")) {
			int lastSlashAt = xpath.lastIndexOf("/@");
			String elementXPath = xpath.substring(0, lastSlashAt);
			String attrName = xpath.substring(lastSlashAt + 2);

			if (elementXPath.isEmpty() || elementXPath.equals("//") || elementXPath.equals("/")
					|| elementXPath.contains("@")) {
				rootElement.setAttribute(attrName, String.valueOf(value));
				return this;
			}

			NodeList elements = evaluateNodeSet(elementXPath);
			if (elements.getLength() == 0) {
				throw new IllegalArgumentException("Element not found: " + elementXPath);
			}
			Element element = (Element) elements.item(0);
			element.setAttribute(attrName, String.valueOf(value));
			return this;
		}

		NodeList nodes = evaluateNodeSet(xpath);
		if (nodes.getLength() == 0) {
			throw new IllegalArgumentException("Element not found for XPath: " + xpath);
		}

		org.w3c.dom.Node node = nodes.item(0);
		if (node.getNodeType() == org.w3c.dom.Node.ATTRIBUTE_NODE) {
			org.w3c.dom.Attr attr = (org.w3c.dom.Attr) node;
			attr.setValue(String.valueOf(value));
		} else if (node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
			Element element = (Element) node;
			element.setTextContent(String.valueOf(value));
		} else {
			throw new IllegalArgumentException("Unsupported node type: " + node.getNodeType());
		}
		return this;
	}

	/**
	 * Sets an attribute on an element specified by an XPath expression.
	 *
	 * @param xpathExpr
	 *            the XPath expression to select the element (e.g., "//author" or
	 *            "//genres/genre[1]")
	 * @param attributeName
	 *            the name of the attribute to set
	 * @param value
	 *            the attribute value to set
	 * @return the current XmlMother instance for method chaining
	 */
	public XmlMother withAttribute(String xpathExpr, String attributeName, @Nullable Object value) {
		if (xpathExpr == null || xpathExpr.isEmpty()) {
			rootElement.setAttribute(attributeName, String.valueOf(value));
			return this;
		}
		if (attributeName == null || attributeName.isEmpty()) {
			throw new IllegalArgumentException("Attribute name cannot be null or empty");
		}

		NodeList nodes = evaluateNodeSet(xpathExpr);
		if (nodes.getLength() == 0) {
			throw new IllegalArgumentException("Element not found for XPath: " + xpathExpr);
		}

		Element element = (Element) nodes.item(0);
		element.setAttribute(attributeName, String.valueOf(value));
		return this;
	}

	/**
	 * Removes an element specified by an XPath expression.
	 *
	 * @param xpathExpr
	 *            the XPath expression to select the element(s) to remove
	 * @return the current XmlMother instance for method chaining
	 */
	public XmlMother withRemovedElement(String xpathExpr) {
		if (xpathExpr == null || xpathExpr.isEmpty()) {
			throw new IllegalArgumentException("XPath expression cannot be null or empty");
		}

		NodeList nodes = evaluateNodeSet(xpathExpr);
		if (nodes.getLength() == 0) {
			throw new IllegalArgumentException("Element not found for XPath: " + xpathExpr);
		}

		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Node parent = node.getParentNode();
			if (parent != null) {
				parent.removeChild(node);
			}
		}

		return this;
	}

	/**
	 * Builds the final XML string based on the current state of the XML structure.
	 *
	 * @return the XML string representation
	 */
	public String build() {
		try {
			var transformerFactory = TransformerFactory.newInstance();
			var transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-attributes", "true");
			var domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException("Error building XML", e);
		}
	}

	private NodeList evaluateNodeSet(String xpathExpr) {
		try {
			return (NodeList) xpath.evaluate(xpathExpr, document, XPathConstants.NODESET);
		} catch (Exception e) {
			throw new RuntimeException("Error evaluating XPath: " + xpathExpr, e);
		}
	}

	private Reader readerForFile(String filePath) {
		var inputStream = this.getClass().getClassLoader().getResourceAsStream(filePath);
		if (inputStream == null) {
			throw new IllegalArgumentException("Unable to open file: " + filePath);
		}
		return new InputStreamReader(inputStream);
	}
}

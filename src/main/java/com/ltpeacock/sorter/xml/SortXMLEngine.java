package com.ltpeacock.sorter.xml;

import static com.ltpeacock.sorter.xml.Util.logAndThrow;
import static com.ltpeacock.sorter.xml.Util.repeat;
import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Engine for sorting XML.
 * @author LieutenantPeacock
 *
 */
public class SortXMLEngine {
    private static final Logger LOG = Logger.getLogger(SortXMLEngine.class.getName());
    private final Comparator<ElementVO> elementComparator;
    private final Comparator<ElementAttribute> attributeComparator;
    private int indent = 2;
    
    /**
     * Constructs a {@code SortXMLEngine} using {@link ElementComparator} for ordering elements
     * and {@link AttributeComparator} for ordering element attributes.
     */
    public SortXMLEngine() {
    	this(new ElementComparator(), new AttributeComparator());
    }
    
    /**
     * Constructs a {@code SortXMLEngine} using the given {@link Comparator}s 
     * for sorting elements and their attributes.
     * <br>
     * For maintaining original element order, use {@link ElementComparator#MAINTAIN_ORDER}
     * and for maintaining original attributes order, use {@link AttributeComparator#MAINTAIN_ORDER}.
     * @param elementComparator The comparator for ordering elements in the XML file
     * @param attributeComparator The comparator for ordering the attributes for each element
     */
    public SortXMLEngine(final Comparator<ElementVO> elementComparator, final Comparator<ElementAttribute> attributeComparator) {
    	this.elementComparator = elementComparator;
    	this.attributeComparator = attributeComparator;
    }
    
    /**
     * Sorts the XML from an {@link InputStream} and prints the result to the given {@link OutputStream}.
     * <br>
     * Both streams will be closed at the end.
     * @param in The InputStream to read the XML document from
     * @param os The OutputStream to write the sorted XML document to
     */
    public void sort(final InputStream in, final OutputStream os) {
        try (BufferedInputStream bis = new BufferedInputStream(in);
                BufferedOutputStream bos = new BufferedOutputStream(os);) {
        	final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
        	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        	int b;
        	while((b = bis.read())!= -1) baos.write(b);
        	final byte[] bytes = baos.toByteArray();
        	final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        	final SAXParser saxParser = saxParserFactory.newSAXParser();
			final AttributesSAXHandler handler = new AttributesSAXHandler();
			saxParser.parse(new ByteArrayInputStream(bytes), handler);
        	final XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(bos, StandardCharsets.UTF_8.name());
        	final Document doc = readXml(new ByteArrayInputStream(bytes));
            final ElementVO root = collectElements(doc.getDocumentElement(), handler.getQueue());
            writer.writeStartDocument();
            sortElement(root, writer, 0);
            writer.writeEndDocument();
            writer.flush();
        } catch (IOException | XMLStreamException | ParserConfigurationException | SAXException e) {
            logAndThrow(e);
        }
    }

    private static String removeExtraWsdlpartClose(final String inputStr) {
        final String s1 = removeExtraClose(inputStr, "wsdl:part");
        final String s2 = removeExtraClose(s1, "wsdl:input");
        final String s3 = removeExtraClose(s2, "wsdl:output");
        final String s4 = removeExtraClose(s3, "xs:element");
        final String s5 = s4.replaceFirst("\\?><wsdl:definitions", "\\?>\r\n<wsdl:definitions");
        final String s6 = s5.replaceAll("\" >", "\">");
        return s6;
    }
    
    private static String removeExtraClose(final String inputStr, final String tag) {
        final Pattern pattern = Pattern.compile("(<" + tag + " .+ )>\\r?\\n?[ \t]*</" + tag + ">", Pattern.MULTILINE);
        final Matcher m = pattern.matcher(inputStr);
        final String prettyXml = m.replaceAll("$1/>");
        return prettyXml;
    }
    
    private ElementVO collectElements(final Element element, final Queue<List<ElementAttribute>> queue) {
    	final ElementVO newElem = new ElementVO(element, queue.poll());
    	final NodeList nodeList = element.getChildNodes();
        final int length = nodeList.getLength();
        final List<ElementVO> children = new ArrayList<>(length);
        for(int i = 0; i < length; i++) {
        	final Node node = nodeList.item(i);
        	if(node.getNodeType() == Node.ELEMENT_NODE) {
        		children.add(collectElements((Element) node, queue));
        	}
        }
        children.sort(elementComparator);
        newElem.childElements = children;
        return newElem;
    }
    
    private void sortElement(final ElementVO element, final XMLStreamWriter writer, final int depth) throws XMLStreamException {
    	writer.writeCharacters(System.lineSeparator());
    	writer.writeCharacters(repeat(" ", indent * depth));
    	writer.writeStartElement(element.getElement().getNodeName());
        final NodeList nodeList = element.getElement().getChildNodes();
        final int length = nodeList.getLength();
        element.attributes.sort(attributeComparator);
        for(final ElementAttribute attribute: element.getAttributes()) {
        	writer.writeAttribute(attribute.getQualifiedName(), attribute.getValue());
        }
        for (int i = 0; i < length; i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                LOG.fine(format("Node type [%s]", currentNode.getNodeType()));
                if(currentNode.getNodeType() == Node.TEXT_NODE) {
                	if(!currentNode.getTextContent().trim().isEmpty()) {
                		final String[] lines = currentNode.getTextContent().split("\\R");
                		for(final String line: lines) {
                			if(!line.trim().isEmpty()) {
	                			writer.writeCharacters(System.lineSeparator());
	                			writer.writeCharacters(repeat(" ", (depth + 1) * indent));
	                			writer.writeCharacters(line.trim());
                			}
                		}
                	}
                } else if(currentNode.getNodeType() == Node.COMMENT_NODE) {
                	writer.writeComment(((Comment) currentNode).getData());
                } else if(currentNode.getNodeType() == Node.CDATA_SECTION_NODE) {
                	writer.writeCData(((CDATASection)currentNode).getData());
                }
            }
        }
        for(final ElementVO child: element.getChildElements()) {
        	sortElement(child, writer, depth + 1);
        }
        writer.writeCharacters(System.lineSeparator());
        writer.writeCharacters(repeat(" ", indent * depth));
        writer.writeEndElement();
    }

    public static void printDocument(final Document doc, final OutputStream out)
            throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        LOG.info("===== printing XML =====");
        transformer.transform(new DOMSource(doc),
             new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }

    private Document readXml(final InputStream is) {
        DocumentBuilderFactory docBuilderFac = DocumentBuilderFactory.newInstance();
        docBuilderFac.setIgnoringElementContentWhitespace(true);
        DocumentBuilder docBuilder = null;
        Document doc = null;
        try {
            docBuilder = docBuilderFac.newDocumentBuilder();
            doc = docBuilder.parse(is);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logAndThrow(e);
        }
        return doc;
    }
    
    /**
     * Set the number of spaces used in indenting the output.
     * @param spaces The number of spaces used for one indent level.
     */
    public void setIndent(final int spaces) {
    	this.indent = spaces;
    }
}

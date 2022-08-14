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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
    private boolean selfClosing = true;
    private boolean preserveWhitespace = false;
    
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
            writer.close();
        } catch (IOException | XMLStreamException | ParserConfigurationException | SAXException e) {
            logAndThrow(e);
        }
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
        newElem.setChildElements(children);
        return newElem;
    }
    
    private void sortElement(final ElementVO element, final XMLStreamWriter writer, final int depth) throws XMLStreamException {
    	final NodeList nodeList = element.getElement().getChildNodes();
        final int length = nodeList.getLength();
        if(!preserveWhitespace) {
    	    writer.writeCharacters(System.lineSeparator());
    	    writer.writeCharacters(repeat(" ", indent * depth));
        }
        boolean selfClose = selfClosing && element.getChildElements().isEmpty();
        for(int i = 0; i < length; i++) {
        	final Node node = nodeList.item(i);
        	if(node.getNodeType() == Node.TEXT_NODE && 
        			!node.getTextContent().trim().isEmpty()
        			|| node.getNodeType() == Node.COMMENT_NODE
        			|| node.getNodeType() == Node.CDATA_SECTION_NODE) {
        		selfClose = false;
        		break;
        	}
        }
    	if(!selfClose)
    	    writer.writeStartElement(element.getElement().getNodeName());
    	else
    		writer.writeEmptyElement(element.getElement().getNodeName());
        element.sortAttributes(attributeComparator);
        for(final ElementAttribute attribute: element.getAttributes()) {
        	writer.writeAttribute(attribute.getQualifiedName(), attribute.getValue());
        }
        for (int i = 0; i < length; i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() != Node.ELEMENT_NODE) {
                LOG.fine(format("Node type [%s]", currentNode.getNodeType()));
                if(currentNode.getNodeType() == Node.TEXT_NODE) {
                	if(preserveWhitespace) {
                		writer.writeCharacters(currentNode.getTextContent());
                	} else if(!currentNode.getTextContent().trim().isEmpty()) {
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
        if(!selfClose) {
        	if(!preserveWhitespace) {
                writer.writeCharacters(System.lineSeparator());
                writer.writeCharacters(repeat(" ", indent * depth));
        	}
            writer.writeEndElement();
        }
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
     * Set the number of spaces used in indenting the output. The default is {@code 2}.
     * @param spaces The number of spaces used for one indent level.
     */
    public void setIndent(final int spaces) {
    	this.indent = spaces;
    }
    
    /**
     * Set whether the output should use self-closing elements where possible.
     * The default is {@code true}.
     * @param selfClosing Whether self-closing elements should be used.
     */
    public void setSelfClosing(final boolean selfClosing) {
    	this.selfClosing = selfClosing;
    }
    
    /**
     * Set whether whitespace in the document should be preserved (i.e., considered significant).
     * The default is {@code false}. 
     * If this is set to {@code true}, indenting will be turned off.
     * @param preserveWhitespace Whether whitespace should be preserved.
     */
    public void setPreserveWhitespace(final boolean preserveWhitespace) {
    	this.preserveWhitespace = preserveWhitespace;
    }
}

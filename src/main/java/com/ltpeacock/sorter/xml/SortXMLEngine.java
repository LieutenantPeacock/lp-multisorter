package com.ltpeacock.sorter.xml;

import static com.ltpeacock.sorter.xml.Util.logAndThrow;
import static com.ltpeacock.sorter.xml.Util.removeEmptyLines;
import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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

    /**
     * Sorts the XML from an {@link InputStream} and prints the result to the given {@link OutputStream}.
     * <br>
     * Both streams will be closed at the end.
     * @param in The InputStream to read the XML document from
     * @param os The OutputStream to write the sorted XML document to
     */
    public void sort(final InputStream in, final OutputStream os) {
        try (BufferedInputStream bis = new BufferedInputStream(in);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                PrintWriter pw = new PrintWriter(bos)) {
            final Document doc = readXml(in);
            final Element rootDocElement = doc.getDocumentElement();
            sortElement(rootDocElement);
            final String prettyXml = XmlPrettyPrint.prettyXml(doc);
            final String prettyXml2 = XmlPrettyPrint.prettyFormat(prettyXml);
            final String prettyXml3 = removeEmptyLines(prettyXml2);
            final String prettyXml4 = prettyXml3.replaceAll("\">", "\" >").replaceAll("\"/>", "\" />");
            final String prettyXml5 = removeExtraWsdlpartClose(prettyXml4);
            pw.print(prettyXml5);
        } catch (IOException e) {
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
    
    private void sortElement(final Element element) {
        final NodeList nodeList = element.getChildNodes();
        final int length = nodeList.getLength();
        final List<Element> elemList0 = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            Node currentNode = nodeList.item(i);
            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                final String nodeName = currentNode.getNodeName();
                LOG.fine(format("localName [%s]", nodeName));
                elemList0.add((Element) currentNode);
                if (currentNode.getChildNodes() != null && currentNode.getChildNodes().getLength() > 0) {
                    sortElement((Element) currentNode);
                }
            } else {
                LOG.fine(format("Node type [%s]", currentNode.getNodeType()));
            }
        }
        Collections.sort(elemList0, new ElementComparator());
        int count = 0;
        for (Node elem : elemList0) {
            count++;
            LOG.fine(format("removing Count [%s], name[%s]", count, elem.getNodeName()));
            element.removeChild(elem);
        }
        count = 0;
        for (Node elem : elemList0) {
            count++;
            LOG.fine(format("Count [%s], name[%s]", count, elem.getNodeName()));
            element.appendChild(elem);
        }
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
}
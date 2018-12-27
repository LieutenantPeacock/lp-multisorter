package com.ltpeacock.sorter.xml;

import static com.ltpeacock.sorter.xml.Util.logAndThrow;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public class XmlPrettyPrint {
	public static String prettyXml(final Document doc) {
		String xmlString = null;
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			// initialize StreamResult with File object to save to file
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			xmlString = result.getWriter().toString();
		} catch (TransformerException e) {
			logAndThrow(e);
		}
		return xmlString;
	}

	public static String prettyFormat(String input, int indent) {
		Source xmlInput = new StreamSource(new StringReader(input));
		StringWriter stringWriter = new StringWriter();
		StreamResult xmlOutput = new StreamResult(stringWriter);
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		transformerFactory.setAttribute("indent-number", indent);
		try {
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(xmlInput, xmlOutput);
		} catch (TransformerException e) {
			logAndThrow(e);
		}
		return xmlOutput.getWriter().toString();
	}

	public static String prettyFormat(String input) {
		return prettyFormat(input, 2);
	}
}

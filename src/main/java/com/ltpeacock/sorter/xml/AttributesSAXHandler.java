package com.ltpeacock.sorter.xml;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX2 event handler for putting all the attributes of each element
 * in the document into a {@link Queue} of {@link List}s of {@link ElementAttribute}s,
 * all in the order that they appear in the document.
 * @author LieutenantPeacock
 *
 */
public class AttributesSAXHandler extends DefaultHandler {
	private final Queue<List<ElementAttribute>> queue = new ArrayDeque<>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		final int attributesLen = attributes.getLength();
		final List<ElementAttribute> attributeList = new ArrayList<>(attributesLen);
		for (int i = 0; i < attributesLen; i++) {
			attributeList.add(new ElementAttribute(attributes.getQName(i), attributes.getValue(i)));
		}
		queue.add(attributeList);
	}

	/**
	 * Get the {@link Queue} of all element attributes.
	 * @return The queue of all element attributes.
	 */
	public Queue<List<ElementAttribute>> getQueue() {
		return queue;
	}
}

package com.ltpeacock.sorter.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

/**
 * Wrapper class for {@link Element} containing 
 * its attributes in the order they appear in the document
 * and its child elements.
 * @author LieutenantPeacock
 *
 */
public class ElementVO {
	private final Element element;
	final List<ElementAttribute> attributes;
	List<ElementVO> childElements = new ArrayList<>();

	public ElementVO(final Element element, final List<ElementAttribute> attributes) {
		this.element = element;
		this.attributes = attributes;
	}

	/**
	 * Get the wrapped {@link org.w3c.dom.Element}.
	 * @return The wrapped element.
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Get a read-only view of the attributes of the element.
	 * @return A {@link List} of attributes for this element.
	 */
	public List<ElementAttribute> getAttributes() {
		return Collections.unmodifiableList(attributes);
	}

	/**
	 * Get a read-only view of the child elements of the element.
	 * @return A {@link List} of child elements for this element.
	 */
	public List<ElementVO> getChildElements() {
		return Collections.unmodifiableList(childElements);
	}
}

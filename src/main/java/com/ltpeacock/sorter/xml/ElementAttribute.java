package com.ltpeacock.sorter.xml;

/**
 * Value object to store the qualified name and value of an attribute on an element.
 * @author LieutenantPeacock
 *
 */
public class ElementAttribute {
	private final String qualifiedName;
	private final String value;

	/**
	 * Creates an {@code ElementAttribute} with a qualified name and a value.
	 * @param qualifiedName The qualified name (including the namespace) of the attribute
	 * @param value The value of the attribute
	 */
	public ElementAttribute(final String qualifiedName, final String value) {
		this.qualifiedName = qualifiedName;
		this.value = value;
	}

	/**
	 * Get the qualified name of the {@code ElementAttribute}.
	 * @return The qualified name of the element attribute.
	 */
	public String getQualifiedName() {
		return qualifiedName;
	}

	/**
	 * Get the value of the {@code ElementAttribute}.
	 * @return The value of the element attribute.
	 */
	public String getValue() {
		return value;
	}
}

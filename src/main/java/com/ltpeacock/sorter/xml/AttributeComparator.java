package com.ltpeacock.sorter.xml;

import java.util.Comparator;

import org.w3c.dom.Node;

/**
 * Comparator for sorting attributes lexicographically by attribute name.
 * <br>
 * Attributes are represented by the class {@link org.w3c.dom.Node}.
 * @author LieutenantPeacock
 *
 */
public class AttributeComparator implements Comparator<Node> {
	/**
	 * Comparator for maintaining original order of attributes on elements.
	 */
	public static final Comparator<Node> MAINTAIN_ORDER = (a,b) -> 0;
	
	@Override
	public int compare(final Node o1, final Node o2) {
		return Util.compare(o1.getNodeName(), o2.getNodeName());
	}
}

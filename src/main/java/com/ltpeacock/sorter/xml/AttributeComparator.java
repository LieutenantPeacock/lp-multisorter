package com.ltpeacock.sorter.xml;

import java.util.Comparator;

/**
 * Comparator for sorting attributes lexicographically by attribute name.
 * <br>
 * See {@link ElementAttribute}.
 * @author LieutenantPeacock
 *
 */
public class AttributeComparator implements Comparator<ElementAttribute> {
	/**
	 * Comparator for maintaining original order of attributes on elements.
	 */
	public static final Comparator<ElementAttribute> MAINTAIN_ORDER = (a,b) -> 0;
	
	@Override
	public int compare(final ElementAttribute o1, ElementAttribute o2) {
		return Util.compare(o1.getQualifiedName(), o2.getQualifiedName());
	}
}

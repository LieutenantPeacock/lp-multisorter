package com.ltpeacock.sorter.xml;

import java.util.Comparator;

import org.w3c.dom.Element;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public class ElementComparator implements Comparator<Element> {

    private static final String NAME = "name";

    public ElementComparator() {
    }

    @Override
    public int compare(final Element arg0, final Element arg1) {
        int c = Util.compare(safeToUpper(
                reverseColumns(arg0.getNodeName())),
                safeToUpper(reverseColumns(arg1.getNodeName())));
        if (0 == c) {
            final String nameAttr0 = arg0.getAttribute(NAME);
            final String nameAttr1 = arg1.getAttribute(NAME);
            c = Util.compare(safeToUpper(nameAttr0), safeToUpper(nameAttr1));
        }
        return c;
    }

    protected String reverseColumns(final String input) {
        final String result;
        if (input == null) {
            result = input;
        } else {
            final int idx = input.indexOf(':');
            if (idx > 0) {
                final String s1 = input.substring(0, idx);
                final String s2 = input.substring(idx + 1);
                result = s2 + ':' + s1;
            } else {
                result = input;
            }
        }
        return result;
    }

    protected String safeToUpper(final String input) {
        final String result;
        if (input == null) {
            result = null;
        } else {
            result = input.toUpperCase();
        }
        return result;
    }
}

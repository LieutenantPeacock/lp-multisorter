package com.ltpeacock.sorter.xml;

import java.util.Comparator;
import java.util.Optional;

import org.w3c.dom.Element;

/**
 * Comparator for sorting {@link Element}s by node name and then by the {@code "name"} attribute.
 * @author LieutenantPeacock
 *
 */
public class ElementComparator implements Comparator<Element> {
    private static final String NAME = "name";
    /**
     * Comparator for maintaining original order of XML elements.
     */
    public static final Comparator<Element> MAINTAIN_ORDER = (a,b) -> 0;

    @Override
    public int compare(final Element arg0, final Element arg1) {
        int c = Util.compare(safeToUpper(
                reverseColumns(arg0.getNodeName())),
                safeToUpper(reverseColumns(arg1.getNodeName())));
        if (c == 0) {
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
        return Optional.ofNullable(input).map(String::toUpperCase).orElse(null);
    }
}

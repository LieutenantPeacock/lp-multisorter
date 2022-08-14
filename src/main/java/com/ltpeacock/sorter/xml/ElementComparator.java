package com.ltpeacock.sorter.xml;

import java.util.Comparator;
import java.util.Optional;

/**
 * Comparator for sorting elements by node name and then by the {@code "name"} attribute.
 * @author LieutenantPeacock
 *
 */
public class ElementComparator implements Comparator<ElementVO> {
    private static final String NAME = "name";
    /**
     * Comparator for maintaining original order of XML elements.
     */
    public static final Comparator<ElementVO> MAINTAIN_ORDER = (a,b) -> 0;

    @Override
    public int compare(final ElementVO arg0, final ElementVO arg1) {
        int c = Util.compare(safeToUpper(
                reverseColumns(arg0.getElement().getNodeName())),
                safeToUpper(reverseColumns(arg1.getElement().getNodeName())));
        if (c == 0) {
            final String nameAttr0 = arg0.getElement().getAttribute(NAME);
            final String nameAttr1 = arg1.getElement().getAttribute(NAME);
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

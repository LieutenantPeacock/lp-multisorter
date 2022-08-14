package com.ltpeacock.sorter.xml;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class meant only for internal use.
 * @author LieutenantPeacock
 *
 */
public class Util {
	private static final Logger LOG = Logger.getLogger(Util.class.getName());
	
    private Util() {
    	throw new IllegalStateException("No instances of Util allowed");
    }
    
    public static int compare(final String s1, final String s2) {
        return compare(s1, s2, false);
    }

    public static  int compare(final String s1, final String s2, final boolean nullGreater) {
        final int result;
        if (s1 == s2) {
            result = 0;
        } else if (s1 == null) {
            result = nullGreater ? 1 : -1;
        } else if (s2 == null) {
            result = nullGreater ? -1 : 1;
        } else {
            result = s1.compareTo(s2);
        }
        
        return result;
    }

    public static String removeEmptyLines(final String inputStr) {
        final Pattern pattern = Pattern.compile("(^[ \t]*$\\r\\n)+", Pattern.MULTILINE);
        final Matcher m = pattern.matcher(inputStr);
        m.replaceAll("\r\n");
        final String prettyXml = m.replaceAll("\r\n");
        final String prettyXml2 = prettyXml.replaceAll("(\\r\\n)+", "\r\n");
        return prettyXml2;
    }
    
    public static void logAndThrow(final Exception e) {
        LOG.log(Level.SEVERE, "Exception", e);
        throw new RuntimeException(e);
    }
    
    public static String repeat(final String str, final int times) {
    	final StringBuilder sb = new StringBuilder(str.length() * times);
    	for(int i = 0; i < times; i++) sb.append(str);
    	return sb.toString();
    }
}

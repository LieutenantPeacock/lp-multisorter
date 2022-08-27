import java.io.IOException;

import com.ltpeacock.sorter.ContentSorter;
import com.ltpeacock.sorter.xml.SortXMLEngine;

/**
 * Main class for sorting XML using {@link SortXMLEngine}.
 * @author LieutenantPeacock
 *
 */
public class XMLSorter extends AbstractSorterCli {
    public static void main(final String[] args) throws IOException {
    	new XMLSorter().run(args);
    }

	@Override
	protected ContentSorter getSortEngine() {
		return new SortXMLEngine();
	}
}

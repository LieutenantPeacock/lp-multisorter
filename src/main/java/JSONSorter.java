import java.io.IOException;

import com.ltpeacock.sorter.ContentSorter;
import com.ltpeacock.sorter.json.SortJSONEngine;

/**
 * Main class for sorting JSON using {@link SortJSONEngine}.
 * @author LieutenantPeacock
 *
 */
public class JSONSorter extends AbstractSorterCli {
	public static void main(final String[] args) throws IOException {
		new JSONSorter().run(args);
	}

	@Override
	protected ContentSorter getSortEngine() {
		return new SortJSONEngine();
	}
}

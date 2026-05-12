import java.io.IOException;

import com.ltpeacock.sorter.ContentSorter;
import com.ltpeacock.sorter.yaml.SortYAMLEngine;

/**
 * Main class for sorting YAML using {@link SortYAMLEngine}.
 * @author LieutenantPeacock
 *
 */
public class YAMLSorter extends AbstractSorterCli {
	public static void main(final String[] args) throws IOException {
		new YAMLSorter().run(args);
	}

	@Override
	protected ContentSorter getSortEngine() {
		return new SortYAMLEngine();
	}
}

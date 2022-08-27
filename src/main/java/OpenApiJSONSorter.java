import java.io.IOException;

import com.ltpeacock.sorter.ContentSorter;
import com.ltpeacock.sorter.json.SortOpenApiJSONEngine;

/**
 * Main class for sorting OpenAPI JSON using {@link SortOpenApiJSONEngine}.
 * @author LieutenantPeacock
 *
 */
public class OpenApiJSONSorter extends AbstractSorterCli {
	public static void main(final String[] args) throws IOException {
		new OpenApiJSONSorter().run(args);
	}

	@Override
	protected ContentSorter getSortEngine() {
		return new SortOpenApiJSONEngine();
	}
}

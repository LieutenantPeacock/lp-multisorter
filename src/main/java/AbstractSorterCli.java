import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ltpeacock.sorter.ContentSorter;

/**
 * Base class for all command line sorters.
 * 
 * @author LieutenantPeacock
 */
abstract class AbstractSorterCli {
	protected final void run(final String[] args) throws IOException {
		switch (args.length) {
		case 2: {
			final File inputFile = new File(args[0]), outputFile = new File(args[1]);
			try (FileInputStream fis = new FileInputStream(inputFile);
					FileOutputStream fos = new FileOutputStream(outputFile)) {
				runImpl(fis, fos, inputFile.toString(), outputFile.toString());
			}
			break;
		}
		case 1:
			if ("--help".equals(args[0])) {
				help();
			} else {
				final File inputFile = new File(args[0]);
				try (FileInputStream fis = new FileInputStream(inputFile)) {
					runImpl(fis, System.out, inputFile.toString(), "stdout");
				}
			}
			break;
		case 0:
			runImpl(System.in, System.out, "stdin", "stdout");
			break;
		default:
			System.err.println("Too many arguments.");
			break;
		}
	}

	private void runImpl(final InputStream is, final OutputStream os, final String inputName, final String outputName)
			throws IOException {
		final long startMs = System.currentTimeMillis();
		if (os != System.out) {
			System.out.println("Running " + getName());
			System.out.format("Reading from '%s' ...%n", inputName);
		}
		getSortEngine().sort(is, os);
		final long endMs = System.currentTimeMillis();
		final long tookMs = endMs - startMs;
		if (os != System.out)
			System.out.format("Wrote to '%s' ..., took %s ms.%n", outputName, tookMs);
	}

	protected abstract ContentSorter getSortEngine();

	protected String getName() {
		return getClass().getSimpleName();
	}

	protected void help() {
		System.out.println("Usage: [inputFile] [outputFile]");
	}
}

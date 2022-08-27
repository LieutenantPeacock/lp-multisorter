import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;

import com.ltpeacock.sorter.json.SortJSONEngine;

public class JSONSorter {
	public static void main(final String[] args) throws IOException {
		switch (args.length) {
		case 2: {
			final File inputFile = new File(args[0]), outputFile = new File(args[1]);
			try (FileInputStream fis = new FileInputStream(inputFile);
					FileOutputStream fos = new FileOutputStream(outputFile)) {
				run(fis, fos, inputFile.toString(), outputFile.toString());
			}
			break;
		}
		case 1:
			if ("--help".equals(args[0])) {
				help();
			} else {
				final File inputFile = new File(args[0]);
				try (FileInputStream fis = new FileInputStream(inputFile)) {
					run(fis, System.out, inputFile.toString(), "stdout");
				}
			}
			break;
		case 0:
			run(System.in, System.out, "stdin", "stdout");
			break;
		default:
			System.err.println("Too many arguments.");
			break;
		}
	}

	private static void run(final InputStream is, final OutputStream os, final String inputName,
			final String outputName) throws IOException {
		final long startMs = System.currentTimeMillis();
		if (os != System.out)
			System.out.format("Reading from '%s' ...%n", inputName);
		final SortJSONEngine engine = new SortJSONEngine(Comparator.naturalOrder());
		engine.sort(is, os);
		final long endMs = System.currentTimeMillis();
		final long tookMs = endMs - startMs;
		if (os != System.out)
			System.out.format("Wrote to '%s' ..., took %s ms.%n", outputName, tookMs);
	}

	private static void help() {
		System.out.println("Usage: [inputFile] [outputFile]");
	}
}

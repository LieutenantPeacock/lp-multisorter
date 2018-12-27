import static com.ltpeacock.sorter.xml.Util.logAndThrow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ltpeacock.sorter.xml.SortXMLEngine;

/**
 * Main class for sorting XML using {@link SortXMLEngine}.
 * @author LieutenantPeacock
 *
 */
public class XMLSorter {
    public static void main(String[] args) {
        switch (args.length) {
        case 2: {
        	final File inputFile = new File(args[0]), outputFile = new File(args[1]);
            try(FileInputStream fis = new FileInputStream(inputFile); 
            		FileOutputStream fos = new FileOutputStream(outputFile)){
            	run(fis, fos, inputFile.toString(), outputFile.toString());
            } catch (IOException e) {
				logAndThrow(e);
			}
            break;
        }
        case 1:
            if ("--help".equals(args[0])) {
                help();
            } else {
            	final File inputFile = new File(args[0]);
                try(FileInputStream fis = new FileInputStream(inputFile)){
                	run(fis, System.out, inputFile.toString(), "stdout");
                } catch (IOException e) {
					logAndThrow(e);
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
    
    private static void run(final InputStream is, final OutputStream os, final String inputName, final String outputName) {
        final long startMs = System.currentTimeMillis();
        System.out.format("Reading from '%s' ...%n", inputName);
        SortXMLEngine engine = new SortXMLEngine();
        engine.sort(is, os);
        final long endMs = System.currentTimeMillis();
        final long tookMs = endMs - startMs;
        System.out.format("Wrote to '%s' ..., took %s ms.%n", outputName, tookMs);
    }

    protected static void help() {
        System.out.println("Usage: [inputFile] [outputFile]");
    }
}

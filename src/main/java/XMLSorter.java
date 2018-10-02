import static java.lang.String.format;
import static com.ltpeacock.sorter.xml.Util.logException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ltpeacock.sorter.xml.SortXmlEngine;

/**
 * 
 * @author LieutenantPeacock
 *
 */
public class XMLSorter {
    private static final Logger LOG = Logger.getLogger(XMLSorter.class.getName());
    
    public static void main(String[] args) {
        switch (args.length) {
        case 2:
            fileInFileOut(args[0], args[1]);
            break;
        case 1:
            if ("--help".equals(args[0])) {
                help();
            } else {
                fileInStdOut(args[0]);
            }
            break;
        case 0:
            stdInStdOut();
            break;
        default:
            System.err.println("Too many arguments.");
            break;
        }
    }
    
    protected static void fileInFileOut(final String inFileName, final String outFileName) {
        final File inFile = new File(inFileName);
        final File outFile = new File(outFileName);
        final long startMs = System.currentTimeMillis();
        boolean success = false;
        try (InputStream is = new FileInputStream(inFile);
                OutputStream os = new FileOutputStream(outFile)) {
            System.out.println(format("Reading from '%s' ...", inFile));
            SortXmlEngine engine = new SortXmlEngine();
            engine.sort(is, os);
            success = true;
        } catch (FileNotFoundException e) {
            logException(e);
        } catch (IOException e) {
            logException(e);
        }
        if (success) {
            final long endMs = System.currentTimeMillis();
            final long tookMs = endMs - startMs;
            System.out.println(format("Wrote to '%s' ..., took %s ms.", outFile, tookMs));
        }
    }

    protected static void fileInStdOut(final String inFileName) {
        final File inFile = new File(inFileName);
        
        final long startMs = System.currentTimeMillis();
        boolean success = false;
        try (InputStream is = new FileInputStream(inFile);
                OutputStream os = System.out) {
            LOG.log(Level.FINE, format("Reading from '%s' ...", inFile));
            SortXmlEngine engine = new SortXmlEngine();
            engine.sort(is, os);
            success = true;
        } catch (FileNotFoundException e) {
            logException(e);
        } catch (IOException e) {
            logException(e);
        }
        if (success) {
            final long endMs = System.currentTimeMillis();
            final long tookMs = endMs - startMs;
            LOG.log(Level.FINE, format("Wrote to 'stdout' ..., took %s ms.", tookMs));
        }
    }

    protected static void stdInStdOut() {
        
        final long startMs = System.currentTimeMillis();
        boolean success = false;
        try (InputStream is = System.in;
                OutputStream os = System.out) {
            LOG.log(Level.FINE, "Reading from 'stdin' ...");
            SortXmlEngine engine = new SortXmlEngine();
            engine.sort(is, os);
            success = true;
        } catch (FileNotFoundException e) {
            logException(e);
        } catch (IOException e) {
            logException(e);
        }
        if (success) {
            final long endMs = System.currentTimeMillis();
            final long tookMs = endMs - startMs;
            LOG.log(Level.FINE, format("Wrote to 'stdout' ..., took %s ms.", tookMs));
        }
    }

    protected static void help() {
        System.out.println("Usage: [inputFile] [outputFile]");
    }
}

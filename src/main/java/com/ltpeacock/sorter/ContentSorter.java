package com.ltpeacock.sorter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface for classes capable of reading content from an {@link InputStream}, sorting it, 
 * and writing the result to an {@link OutputStream}.
 * @author LieutenantPeacock
 */
public interface ContentSorter {
	/**
	 * Sort the content from the given {@link InputStream} and write it to the given {@link OutputStream}.
	 * @param is The {@link InputStream} to read the content from.
	 * @param os The {@link OutputStream} to write the sorted result to.
	 * @throws IOException If there is an error in reading/writing data.
	 */
	void sort(InputStream is, OutputStream os) throws IOException;
}

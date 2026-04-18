package com.ltpeacock.sorter.json;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import com.github.openjson.JSONTokener;
import com.ltpeacock.sorter.ContentSorter;

/**
 * Engine for sorting JSON by key name.
 * @author LieutenantPeacock
 *
 */
public class SortJSONEngine implements ContentSorter {
	private final Comparator<String> keyComparator;
	private boolean recursive = true;
	private int indent = 4;
	
	/**
	 * Constructs a {@code SortJSONEngine} that sorts the keys in lexicographical order.
	 */
	public SortJSONEngine() {
		this(Comparator.naturalOrder());
	}

	/**
	 * Constructs a {@code SortJSONEngine} that sorts the keys using the {@code keyComparator}.
	 * @param keyComparator The {@link Comparator} for sorting object keys.
	 */
	public SortJSONEngine(final Comparator<String> keyComparator) {
		this.keyComparator = keyComparator;
	}
	
	/**
	 * Sorts the JSON from an {@link InputStream} and prints the result to the given {@link OutputStream}.
	 * @param is The InputStream to read the JSON file from.
	 * @param os The OutputStream to write the sorted JSON to.
	 * @throws IOException If there is an error in reading/writing data.
	 */
	@Override
	public void sort(InputStream is, OutputStream os) throws IOException {
		if (!(os instanceof BufferedOutputStream)) os = new BufferedOutputStream(os);
		final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		final Object value = new JSONTokener(reader).nextValue();
		final String output;
		if (value instanceof JSONObject) {
			sort((JSONObject) value);
			output = ((JSONObject) value).toString(indent);
		} else if (value instanceof JSONArray) {
			sort((JSONArray) value);
			output = ((JSONArray) value).toString(indent);
		} else if (value instanceof String) {
			output = JSONObject.quote((String) value);
		} else if (value instanceof Number) {
			output = JSONObject.numberToString((Number) value);
		} else {
			output = String.valueOf(value);
		}
		os.write(output.getBytes(StandardCharsets.UTF_8));
		os.flush();
	}

	/**
	 * Sorts the keys of a {@link JSONObject}.
	 * @param obj The object to sort.
	 */
	public void sort(final JSONObject obj) {
		final String[] keys = JSONObject.getNames(obj);
		Arrays.sort(keys, keyComparator);
		final Object[] values = Arrays.stream(keys).map(obj::get).toArray();
		for (int i = 0; i < keys.length; i++) {
			obj.remove(keys[i]);
			obj.put(keys[i], values[i]);
			if (recursive) {
				if (values[i] instanceof JSONObject) {
					sort((JSONObject) values[i]);
				} else if (values[i] instanceof JSONArray) {
					sort((JSONArray) values[i]);
				}
			}
		}
	}

	/**
	 * Sorts the keys of all objects in the given {@link JSONArray}.
	 * @param arr The JSONArray.
	 */
	public void sort(final JSONArray arr) {
		for (final Object obj : arr) {
			if (obj instanceof JSONObject) {
				sort((JSONObject) obj);
			} else if (recursive && obj instanceof JSONArray) {
				sort((JSONArray) obj);
			}
		}
	}

	/**
	 * Set whether the sorting should be recursive. 
	 * If set to {@code true}, the keys of all objects contained within the root object
	 * will also be sorted, and arrays within arrays will also have the keys of 
	 * all objects within them sorted.
	 * The default is {@code true}.
	 * @param recursive Whether or not the sorting should be recursive.
	 */
	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	/**
	 * Set the amount of indent used when outputting the JSON. The default is {@code 4}.
	 * @param indent The number of spaces used for one indent level.
	 */
	public void setIndent(int indent) {
		this.indent = indent;
	}
}
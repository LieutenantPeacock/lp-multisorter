package com.ltpeacock.sorter.json;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

public class SortJSONEngine {
	private final Comparator<String> keyComparator;
	private boolean recursive = true;
	private int indent = 4;
	
	public SortJSONEngine() {
		this(Comparator.naturalOrder());
	}

	public SortJSONEngine(final Comparator<String> keyComparator) {
		this.keyComparator = keyComparator;
	}
	
	public void sort(InputStream is, OutputStream os) throws IOException {
		if(!(is instanceof BufferedInputStream)) is = new BufferedInputStream(is);
		if(!(os instanceof BufferedOutputStream)) os = new BufferedOutputStream(os);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for(int b; (b = is.read()) != -1;) baos.write(b);
		final String str = baos.toString(StandardCharsets.UTF_8.name());
		if(str.trim().startsWith("{")) {
			final JSONObject obj = new JSONObject(str);
			sort(obj);
			os.write(obj.toString(indent).getBytes(StandardCharsets.UTF_8.name()));
		} else {
			final JSONArray arr = new JSONArray(str);
			sort(arr);
			os.write(arr.toString(indent).getBytes(StandardCharsets.UTF_8.name()));
		}
		os.flush();
	}

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

	public void sort(final JSONArray arr) {
		for (final Object obj : arr) {
			if (obj instanceof JSONObject) {
				sort((JSONObject) obj);
			} else if (recursive && obj instanceof JSONArray) {
				sort((JSONArray) obj);
			}
		}
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}
}
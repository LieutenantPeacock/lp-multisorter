package com.ltpeacock.sorter.json;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

/**
 * Engine for sorting OpenAPI JSON.
 * @author LieutenantPeacock
 *
 */
public class SortOpenApiJSONEngine {
	private int indent = 4;

	/**
	 * Sorts the OpenAPI JSON from an {@link InputStream} and prints the result to the given {@link OutputStream}.
	 * @param is The InputStream to read the OpenAPI JSON file from.
	 * @param os The OutputStream to write the sorted JSON to.
	 * @throws IOException If there is an error in reading/writing data.
	 */
	public void sort(InputStream is, OutputStream os) throws IOException {
		if (!(is instanceof BufferedInputStream))
			is = new BufferedInputStream(is);
		if (!(os instanceof BufferedOutputStream))
			os = new BufferedOutputStream(os);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		for (int b; (b = is.read()) != -1;)
			baos.write(b);
		final JSONObject sorted = sort(new JSONObject(baos.toString(StandardCharsets.UTF_8.name())));
		os.write(sorted.toString(indent).getBytes(StandardCharsets.UTF_8));
		os.flush();
	}

	/**
	 * Sorts a {@code JSONObject}.
	 * @param obj The object to sort.
	 * @return The sorted object.
	 */
	public JSONObject sort(final JSONObject obj) {
		final JSONObject sortedObj = new JSONObject();
		final Object api = obj.get("openapi");
		final JSONObject info = (JSONObject) obj.get("info");
		final JSONArray serversOut = new JSONArray();
		final JSONArray servers = (JSONArray) obj.get("servers");
		final int serversLen = servers.length();
		for (int i = 0; i < serversLen; i++) {
			final JSONObject outObject = new JSONObject();
			final JSONObject o = servers.getJSONObject(i);
			outObject.put("url", o.get("url"));
			outObject.put("description", o.get("description"));
			serversOut.put(outObject);
		}
		sortedObj.put("openapi", api);
		sortedObj.put("info", info);
		sortedObj.put("servers", serversOut);
		final JSONObject pathsOut = sortPaths(obj.getJSONObject("paths"));
		sortedObj.put("paths", pathsOut);
		final JSONObject componentNew = new JSONObject();
		final JSONObject component = obj.getJSONObject("components");
		final JSONObject schema = component.getJSONObject("schemas");
		final JSONArray schemaNames = schema.names();
		final List<String> nameList = sortNames(schemaNames);
		final JSONObject schemaNew = new JSONObject();
		for (int i = 0; i < nameList.size(); i++) {
			final String key = nameList.get(i);
			final JSONObject schemaItem = schema.getJSONObject(key);
			schemaNew.put(key, schemaItem);
		}
		componentNew.put("schemas", schemaNew);
		sortedObj.put("components", componentNew);
		return sortedObj;
	}

	private JSONObject sortPaths(final JSONObject paths) {
		final JSONArray names = paths.names();
		final int sizeOfPaths = names.length();
		final List<String> nameList = sortNames(names);
		final JSONObject pathsOut = new JSONObject();
		for (int i = 0; i < sizeOfPaths; i++) {
			final String key = nameList.get(i);
			final JSONObject pathNew = new JSONObject();
			pathsOut.put(key, pathNew);
			final JSONObject o = paths.getJSONObject(key);
			final Iterator<String> it = o.keys();
			while (it.hasNext()) {
				final String ob = it.next();
				final JSONObject underAction = o.getJSONObject(ob);
				final JSONObject underActionNew = new JSONObject();
				pathNew.put(ob, underActionNew);
				final Iterator<String> itUnderAction = underAction.keys();
				while (itUnderAction.hasNext()) {
					final String keyUnterAction = itUnderAction.next();
					final Object objUnderAction = underAction.get(keyUnterAction);
					if ("responses".equals(keyUnterAction)) {
						final JSONObject responses = (JSONObject) objUnderAction;
						final JSONObject responsesNew = new JSONObject();
						final JSONArray responseCodes = responses.names();
						final List<String> codeList = sortNames(responseCodes);
						for (int j = 0; j < codeList.size(); j++) {
							final String codeStr = codeList.get(j);
							final Object codeObj = responses.get(codeStr);
							responsesNew.put(codeStr, codeObj);
						}
						underActionNew.put("responses", responsesNew);
					} else {
						underActionNew.put(keyUnterAction, objUnderAction);
					}
				}
			}
		}
		return pathsOut;
	}

	private List<String> sortNames(final JSONArray names) {
		final int sizeOfPaths = names.length();
		final List<String> nameList = new ArrayList<>(sizeOfPaths);
		for (int i = 0; i < sizeOfPaths; i++) {
			final String o = (String) names.get(i);
			nameList.add(o);
		}
		Collections.sort(nameList);
		return nameList;
	}

	/**
	 * Set the amount of indent used when outputting the JSON. The default is {@code 4}.
	 * @param indent The number of spaces used for one indent level.
	 */
	public void setIndent(final int indent) {
		this.indent = indent;
	}
}

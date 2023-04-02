/*******************************************************************************
 * Copyright (c) 2016-2022 by G. Weirich
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *
 * Contributors:
 * G. Weirich - initial implementation
 *********************************************************************************/

package ch.elexis.ungrad.lucinda;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class Client3 {
	private String api = "/lucinda/3.0";
	private Logger log = LoggerFactory.getLogger("Lucinda v3 client");
	private HttpURLConnection conn;

	private URL makeURL(final String call) throws MalformedURLException {
		String server = Preferences.get(Preferences.SERVER_ADDR, "127.0.0.1"); //$NON-NLS-1$
		int port = Integer.parseInt(Preferences.get(Preferences.SERVER_PORT, "9997")); //$NON-NLS-1$
		return new URL("http://" + server + ":" + port + api + call);

	}

	private byte[] doGet(final String api_call) throws IOException {
		URL url = makeURL(api_call);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("method", "get");
		conn.setConnectTimeout(5000);
		int response = conn.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedInputStream bin = new BufferedInputStream(conn.getInputStream());
			int c;
			while ((c = bin.read()) != -1) {
				baos.write(c);
			}
			return baos.toByteArray();
		} else {
			throw new IOException("could not read " + api_call + ": Status was " + response);
		}
	}

	private String doPost(final String api_call, final String body, final int expectedStatus) throws IOException {
		URL url = makeURL(api_call);
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("method", "post");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Content-Length", String.valueOf(body.length()));
		// conn.setConnectTimeout(5000);
		conn.setDoOutput(true);

		OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
		os.write(body);
		os.flush();
		int response = conn.getResponseCode();
		if (response == expectedStatus) {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			StringBuffer buffer = new StringBuffer();
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			in.close();
			conn.disconnect();
			return buffer.toString();
		} else {
			log.error("Bad answer for " + api_call + ": " + response);
			return null;
		}
	}

	public void analyzeFile(final byte[] contents, INotifier got) throws IOException {
		URL url = makeURL("/parse");
		conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		// conn.setRequestProperty("method", "post");
		conn.setRequestProperty("Content-Type", "application/octet-stream");
		conn.setRequestProperty("Content-Length", String.valueOf(contents.length));
		// conn.setConnectTimeout(5000);
		conn.setDoOutput(true);

		OutputStream os = conn.getOutputStream();
		os.write(contents);
		os.flush();
		int response = conn.getResponseCode();
		if (response == 200) {
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			// StringBuffer buffer = new StringBuffer();
			while ((line = in.readLine()) != null) {
				if (got.received(line)) {
					break;
				}
				// buffer.append(line);
			}
			in.close();
			conn.disconnect();
			// return buffer.toString();
		} else {
			log.error("Bad answer for parse: " + response);
			// return null;
		}
	}

	public Map query(final String phrase) throws Exception {

		HashMap<String, Object> params = new HashMap<>();
		HashMap<String, Object> query = new HashMap<>();
		HashMap<String, Object> edismax = new HashMap<>();
		edismax.put("query", phrase);
		query.put("edismax", edismax);
		params.put("query", query);
		params.put("limit", Preferences.get(Preferences.MAXIMUM_HITS, "100"));
		String result = doPost("/query", writeJson(params), HttpURLConnection.HTTP_OK);
		if (result == null) {
			throw (new Exception("Connection problem"));
		} else {
			Map<String, Object> json = new HashMap<String, Object>();
			json.put("result", readJson(result).get("docs"));
			json.put("status", "ok");
			return json;
		}

	}

	public Map get(final String id) throws Exception {
		Map<String, Object> json = new HashMap<String, Object>();
		byte[] result = doGet("/get/" + id);
		json.put("status", "ok");
		json.put("result", result);
		return json;
	}

	public void rescan() {

	}

	/**
	 * Add a document to the index. Note: The document itself will not be stored,
	 * only parsed and added to the index. The caller must handle it by itself and
	 * make sure, that it can retrieve the document with the given id
	 *
	 * @param id       unique id for the document. The caller should be able to
	 *                 retrieve or reconstruct the document later with this id
	 * @param title    A title for the document.
	 * @param doctype  a random document type (this is not the mime-type but rather
	 *                 some application dependent organizational attribute)
	 * @param metadata application defined metadata. These are stored with the index
	 *                 and can be queried for. Example: If there is an attribute
	 *                 "author: john doe", a later query can search for "author:
	 *                 john*"
	 * @param contents The file contents parse. Many file types are supported and
	 *                 recognized by content (not by file extension), such as .odt,
	 *                 .doc, .pdf, tif. Image files are parsed through OCR and any
	 *                 found text is indexed
	 * @param handler  Handler to call after indexing
	 */
	public Map addToIndex(final String id, final String title, final String doctype, Map metadata,
			final byte[] contents) throws Exception {
		Map<String, Object> params = prepare(id, title, doctype, metadata);
		params.put("contents", contents);

		String ans = doPost("/addindex", writeJson(params), HttpURLConnection.HTTP_CREATED);
		if (StringTool.isNothing(ans)) {
			throw (new Exception("Empty response"));
		} else {
			Map<String, Object> answer = readJson(ans);
			Map<String, Object> result = (Map<String, Object>) answer.get("responseHeader");
			if (result.get("status") == (Integer) 0) {
				result.put("status", "ok");
				result.put("_id", id);
			}
			return result;
		}

	}

	/**
	 * Add a file to the Lucinda store and index.
	 * 
	 * @param filename Name for the file to write (no path, only filename)
	 * @param concern  some grouping hint for the file (e.g. name of the group). The
	 *                 file will be stored in a subdirectory of that name. if
	 *                 concern is null, the base directory for imports is used.
	 * @param doctype  a random document type (this is not the mime-type but rather
	 *                 some application dependent organizational attribute)
	 * @param metadata application defined metadata. These are stored with the index
	 *                 and can be queried for. Example: If there is an attribute
	 *                 "author: john doe", a later query can search for "author:
	 *                 john*"
	 * @param contents The file contents to parse. Many file types are supported and
	 *                 recognized by content (not by file extension), such as .odt,
	 *                 .doc, .pdf, tif. Image files are parsed through OCR and any
	 *                 found text is indexed
	 * @param handler  Handler to call after the import
	 * @throws IOException
	 */
	public Map addFile(final String uid, final String filename, final String concern, final String doctype,
			Map<String, Object> metadata, final byte[] contents) throws Exception {
		Map<String, Object> meta = prepare(uid, filename, doctype, metadata);
		if (!StringTool.isNothing(concern)) {
			meta.put("concern", concern);
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("metadata", meta);
		params.put("payload", contents);
		String ans = doPost("/addindex", writeJson(params), HttpURLConnection.HTTP_ACCEPTED);
		if (StringTool.isNothing(ans)) {
			throw new Exception("Empty response adding file " + filename);
		} else {
			Map<String, Object> answer = readJson(ans);
			Map<String, Object> result = (Map<String, Object>) answer.get("resultHeader");
			return result;
		}

	}

	/*
	 * public void shutDown() { }
	 */

	private Map<String, Object> prepare(final String id, final String title, final String doctype,
			Map<String, Object> metadata) {
		if (metadata == null) {
			metadata = new HashMap<String, Object>();
		}
		if (!StringTool.isNothing(id)) {
			metadata.put("id", id);
		}
		metadata.put("title", title);
		metadata.put("lucinda_doctype", doctype);
		metadata.put("filename", title);
		return metadata;
	}

	/*
	 * syntactic sugar to create and initialize a JsonObject with a single call
	 */
	private Map make(String... params) {
		Map<String, Object> ret = new HashMap<>();
		for (String param : params) {
			String[] p = param.split(":");
			ret.put(p[0], p[1]);
		}
		return ret;
	}

	public Map<String, Object> readJson(String source) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Map<String, Object> res = mapper.readValue(source, new TypeReference<Map<String, Object>>() {
			});
			return res;
		} catch (Exception e) {
			return null;
		}
	}

	public String writeJson(Map<String, Object> source) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(source);
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}

	}

	public interface INotifier {
		public boolean received(String text);
	}

}

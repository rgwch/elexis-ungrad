/*******************************************************************************
idea * Copyright (c) 2016 by G. Weirich
 * <p/>
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * <p/>
 * Contributors:
 * G. Weirich - initial implementation
 ******************************************************************************/

package ch.elexis.ungrad.lucinda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * Client library for Lucinda Created by gerry on 10.05.16.
 */
public class Client {
	private String api = "/api/1.0/";
	private Vertx vertx;
	private Logger log = Logger.getLogger("Lucinda Client");
	private HttpClient http;
	private VertxOptions vertxOptions = new VertxOptions().setMaxEventLoopExecuteTime(10000000000L)
			.setBlockedThreadCheckInterval(3000);

	/**
	 * Connect to a Lucinda Server (REST only. Server address provided)
	 *
	 * @param server_ip
	 *            Address of the server
	 * @param port
	 *            Port to connect
	 * @param handler
	 *            The handler to call for lucinda related messages
	 */
	public void connect(final String server_ip, final int port, final Handler handler) {
		vertx = Vertx.vertx(vertxOptions);
		HttpClientOptions hop = new HttpClientOptions().setDefaultHost(server_ip).setDefaultPort(port)
				.setTryUseCompression(true).setKeepAlive(false).setIdleTimeout(300);
		http = vertx.createHttpClient(hop);
		HttpClientRequest htr = http.request(HttpMethod.GET, api + "ping", response -> {
			response.bodyHandler(buffer -> {
				if (buffer.toString().equals("pong")) {
					handler.signal(make("status:connected"));
					log.info("Rest API ok");
				}
			});
		}).setTimeout(5000L).exceptionHandler(exception -> {
			log.severe("REST failure " + exception.getMessage());
			exception.printStackTrace();
			handler.signal(make("status:failure", "message:" + exception.getMessage()));
		});
		htr.end();
	}

	/**
	 * Search the index
	 *
	 * @param queryPhrase
	 *            What to search
	 * @param handler
	 */
	public void query(final String queryPhrase, final Handler handler) {
		http.post(api + "query", response -> {
			if (response.statusCode() == 200) {
				response.bodyHandler(buffer -> {
					JsonObject result = buffer.toJsonObject();
					handler.signal(result);
				});
			} else if (response.statusCode() == 204) { // Empty result
				JsonObject result = make("status:ok").put("result", new ArrayList<Object>());
				handler.signal(result);
			} else {
				handler.signal(make("status:error", "message:" + response.statusMessage()));
			}
		}).end(queryPhrase);

	}

	/**
	 * retrieve a document from its id
	 *
	 * @param id
	 *            uinique id of the document
	 * @param handler
	 */
	public void get(final String id, final Handler handler) {
		http.getNow(api + "get/" + id, response -> {
			if (response.statusCode() == 200) {
				response.bodyHandler(buffer -> {
					byte[] file = buffer.getBytes();
					handler.signal(make("status:ok").put("result", file));
				});
			} else if (response.statusCode() == 404) {
				handler.signal(make("status:not found"));
			} else {
				handler.signal(make("status:error", "message:" + response.statusMessage()));
			}
		});
	}

	/**
	 * Add a document to the index. Note: The document itself will not be
	 * stored, only parsed and added to the index. The caller must handle it by
	 * itself and make sure, that it can retrieve the document with the given id
	 *
	 * @param id
	 *            unique id for the document. The caller should be able to
	 *            retrieve or reconstruct the document later with this id
	 * @param title
	 *            A title for the document.
	 * @param doctype
	 *            a random document type (this is not the mime-type but rather
	 *            some application dependent organizational attribute)
	 * @param metadata
	 *            application defined metadata. These are stored with the index
	 *            and can be queried for. Example: If there is an attribute
	 *            "author: john doe", a later query can search for "author:
	 *            john*"
	 * @param contents
	 *            The file contents parse. Many file types are supported and
	 *            recognized by content (not by file extension), such as .odt,
	 *            .doc, .pdf, tif. Image files are parsed through OCR and any
	 *            found text is indexed
	 * @param handler
	 *            Handler to call after indexing
	 */
	public void addToIndex(final String id, final String title, final String doctype, JsonObject metadata,
			final byte[] contents, final Handler handler) {

		try {
			JsonObject envelope = prepare(id, title, doctype, metadata, contents);
			http.post(api + "index", response -> {
				response.bodyHandler(buffer -> {
					JsonObject answer = buffer.toJsonObject();
					handler.signal(answer);

				});
			}).end(envelope.encode());

		} catch (IOException ex) {
			ex.printStackTrace();
			handler.signal(make("status:error", "message:" + ex.getMessage()));
		}

	}

	/**
	 * Add a file to the Lucinda store and index.
	 *
	 * @param filename
	 *            Name for the file to write (no path, only filename)
	 * @param concern
	 *            some grouping hint for the file (e.g. name of the group). The
	 *            file will be stored in a subdirectory of that name. if concern
	 *            is null, the base directory for imports is used.
	 * @param doctype
	 *            a random document type (this is not the mime-type but rather
	 *            some application dependent organizational attribute)
	 * @param metadata
	 *            application defined metadata. These are stored with the index
	 *            and can be queried for. Example: If there is an attribute
	 *            "author: john doe", a later query can search for "author:
	 *            john*"
	 * @param contents
	 *            The file contents parse. Many file types are supported and
	 *            recognized by content (not by file extension), such as .odt,
	 *            .doc, .pdf, tif. Image files are parsed through OCR and any
	 *            found text is indexed
	 * @param handler
	 *            Handler to call after the import
	 */
	public void addFile(final String uid, final String filename, final String concern, final String doctype,
			JsonObject metadata, final byte[] contents, final Handler handler) {

		try {
			JsonObject envelope = prepare(uid, filename, doctype, metadata, contents);
			if (concern != null) {
				envelope.put("concern", concern);
			}
			http.post(api + "addfile", response -> {
				response.bodyHandler(buffer -> {
					JsonObject result = buffer.toJsonObject();
					handler.signal(result);
				});
			}).end(envelope.encode());

		} catch (IOException ex) {
			ex.printStackTrace();
			handler.signal(make("status:error", "message:" + ex.getMessage()));

		}
	}

	private JsonObject prepare(final String id, final String title, final String doctype, JsonObject metadata,
			final byte[] contents) throws IOException {
		if (metadata == null) {
			metadata = new JsonObject();
		}
		if (!id.isEmpty()) {
			metadata.put("_id", id);
		}
		metadata.put("title", title);
		metadata.put("lucinda_doctype", doctype);
		metadata.put("filename", title);
		metadata.put("payload", contents);
		return metadata;
	}

	public void shutDown() {
		vertx.close();
		http.close();
	}

	/*
	 * syntactic sugar to create and initialize a JsonObject with a single call
	 */
	private JsonObject make(String... params) {
		JsonObject ret = new JsonObject();
		for (String param : params) {
			String[] p = param.split(":");
			ret.put(p[0], p[1]);
		}
		return ret;
	}

}

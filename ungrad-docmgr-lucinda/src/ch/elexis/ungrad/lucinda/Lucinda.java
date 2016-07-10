/*******************************************************************************
 * Copyright (c) 2016 by G. Weirich
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

import java.util.Map;

import ch.rgw.io.FileTool;

public class Lucinda {
	private Client client;
	private boolean connected = false;

	public Lucinda() {
		client = new Client();
	}

	public void connect(final Handler handler) {
		if (!connected) {
			String server = Preferences.get(Preferences.SERVER_ADDR, "127.0.0.1"); //$NON-NLS-1$
			int port = Integer.parseInt(Preferences.get(Preferences.SERVER_PORT, "2016")); //$NON-NLS-1$
			client.connect(server, port, handler);
		}
	}

	public void disconnect() {
		if (connected) {
			connected = false;
			if (client != null) {
				client.shutDown();
			}
		}
	}

	public void query(String q, Handler ha) {
		client.query(q, ha);
	}

	public void get(String id, Handler ha) {
		client.get(id, ha);
	}

	public void addToIndex(String id, String title, String type, Map<String, Object> meta, byte[] contents,
			Handler handler, boolean bCopy) {
		if (bCopy) {
			String filetype = (String) meta.get("filetype");
			if (filetype != null) {
				String ext = FileTool.getExtension(filetype);
				if (ext.length() == 0) {
					ext = filetype;
				}
				title += "."+ext;
			}
			client.addFile(id, title, (String) meta.get("concern"), type, meta, contents, handler);
		} else {
			client.addToIndex(id, title, type, meta, contents, handler);
		}
	}

}

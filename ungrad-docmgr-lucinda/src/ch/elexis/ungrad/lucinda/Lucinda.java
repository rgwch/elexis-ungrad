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

import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.lucinda.Client;
import ch.rgw.lucinda.Handler;
import ch.rgw.tools.StringTool;

public class Lucinda {
	private Client client;
	private boolean connected = false;
	private boolean RestAPI = false;
	private boolean BusAPI = false;

	public Lucinda() {
		client = new Client();
	}

	public void connect(final Handler handler) {
		Handler primary = new LucindaHandler(handler);
		if (Preferences.get(Preferences.SERVER_ADDR, "") != "") { //$NON-NLS-1$ //$NON-NLS-2$
			connectRest(primary);
		} else {
			connectBus(primary);
		}
	}

	public void connectRest(final Handler handler) {
		if (!connected) {
			String server = Preferences.get(Preferences.SERVER_ADDR, "127.0.0.1"); //$NON-NLS-1$
			int port = Integer.parseInt(Preferences.get(Preferences.SERVER_PORT, "2016")); //$NON-NLS-1$
			client.connect(server, port, handler);
		}
	}

	public void connectBus(final Handler handler) {
		if (!connected) {
			String prefix = Preferences.get(Preferences.MSG, "ch.rgw.lucinda"); //$NON-NLS-1$
			String network = Preferences.get(Preferences.NETWORK, ""); //$NON-NLS-1$
			if (network.isEmpty()) {
				String server = Preferences.get(Preferences.SERVER_ADDR, "127.0.0.1"); //$NON-NLS-1$
				if (!server.isEmpty()) {
					String[] srv = server.split("\\.");
					if (srv.length == 4) {
						srv[3] = "*";
						network = StringTool.join(srv, ".");
					}
				}
			}
			client.connect(prefix, network, handler);
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

	public boolean isRestAPI() {
		return RestAPI;
	}

	public boolean isBusAPI() {
		return BusAPI;
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

			client.addFile(id, title, type, meta, contents, handler);
		} else {
			client.addToIndex(id, title, type, meta, contents, handler);
		}
	}

	class LucindaHandler implements Handler {
		private Handler secondLevel;

		LucindaHandler(final Handler second) {
			secondLevel = second;
		}

		@Override
		public void signal(final Map<String, Object> result) {
			switch ((String) result.get("status")) { //$NON-NLS-1$

			case "connected": //$NON-NLS-1$
				connected = true;
				break;
			case "Rest OK":
				RestAPI = true;
				break;
			case "disconnected": //$NON-NLS-1$
				connected = false;
				RestAPI = false;
				break;
			case "failure": //$NON-NLS-1$
				SWTHelper.showInfo("Lucinda", (String) result.get("message")); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			case "error": //$NON-NLS-1$
				SWTHelper.showError("Lucinda", Messages.Activator_Lucinda_error_caption, //$NON-NLS-1$
						Messages.Activator_Server_Message + result.get("message")); //$NON-NLS-1$
				break;

			default:
				SWTHelper.showError("Lucinda", "Lucinda", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.Activator_unexpected_answer + result.get("status") + Messages.Activator_14 //$NON-NLS-1$
								+ result.get("message")); //$NON-NLS-1$ //$NON-NLS-3$
			}

			secondLevel.signal(result);
		}

	}

}

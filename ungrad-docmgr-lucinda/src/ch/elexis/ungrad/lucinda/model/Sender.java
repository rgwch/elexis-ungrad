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

package ch.elexis.ungrad.lucinda.model;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;
import ch.elexis.ungrad.lucinda.Handler;
import ch.elexis.ungrad.lucinda.Lucinda;
import io.vertx.core.json.JsonObject;

/**
 * The Sender takes a List of PersistentObjects from its creating customer and
 * sends them asynchroneously to Lucinda. Each Object gets "specified" by the
 * customer, i.e. the customer transforms the PersistentObject into a map of
 * properties to persist.
 * 
 * @author gerry
 * 
 */
public class Sender implements Handler {
	private List<? extends PersistentObject> toDo;
	private Customer customer;
	private List<JsonObject> answers = new ArrayList<>();
	private List<String> onTheWay = new ArrayList<String>();
	private Lucinda lucinda;
	private Logger log = LoggerFactory.getLogger(getClass());
	private boolean bCopy = false;

	/**
	 * Create a new Sender. It will run immediately after creation
	 * 
	 * @param customer
	 *            the customer who is able to specify documents to import to
	 *            lucinda
	 * @param list
	 *            List of PersistenObjects to send
	 * @param bCopy
	 *            if false, Documents are only indexed. If true, Documents are
	 *            imported to Lucinda
	 */
	public Sender(Customer customer, List<? extends PersistentObject> list, boolean bCopy) {
		toDo = list;
		this.customer = customer;
		this.bCopy = bCopy;
		lucinda = new Lucinda();
		lucinda.connect(result -> {
			if (result.getString("status").equalsIgnoreCase("connected")) {
				sendNext();
			} else {
				SWTHelper.showError("Lucinda",
						"unexpected answer " + result.getString("status") + ", " + result.getString("message"));
			}
		});
	}

	/**
	 * Lucinda will signal each transfer of a Document. We use these signals to
	 * keep track of how many Documents are still on the way. If no more
	 * Documents are waiting and no more Documents are on the way, then we tell
	 * our customer, that we finished the job.
	 */
	@Override
	public void signal(JsonObject message) {
		String id = message.getString("_id"); //$NON-NLS-1$
		if (!message.getString("status").equals("ok")) {
			SWTHelper.showError("Lucinda Sender " + message.getString("status"),
					message.getString("message") + "; " + message.getString("title"));
		} else {
			customer.success(id);
		}
		onTheWay.remove(id);
		answers.add(message);
		if (toDo.isEmpty() && onTheWay.isEmpty()) {
			customer.finished(answers);
			lucinda.disconnect();
		}
		sendNext();
	}

	/*
	 * Send the next Document to Lucinda. We don't check for duplicates here,
	 * since Lucinda won't add duplicates to the index. (It will update existing
	 * documents instead)
	 */
	private void sendNext() {
		if (!toDo.isEmpty()) {
			PersistentObject po = toDo.remove(0);
			if (po.exists()) {
				JsonObject order = customer.specify(po);
				if (order == null) {
					toDo.clear();
				} else {
					byte[] contents = order.getBinary("payload"); //$NON-NLS-1$
					if (contents != null) { // skip empty files - no point in
											// indexing null
						String title = order.getString("title");
						String type = order.getString("type");
						log.debug(title);
						onTheWay.add(po.getId());
						lucinda.addToIndex(po.getId(), title == null ? "?" : title, //$NON-NLS-1$
								type == null ? "" : type, order, contents, this, bCopy); //$NON-NLS-1$
					} else { // Skipped empty file -> Advance to next
						log.warn("Skipping empty Document " + order.getString("title"));
						sendNext();
					}
				}
			}
		}
	}
}
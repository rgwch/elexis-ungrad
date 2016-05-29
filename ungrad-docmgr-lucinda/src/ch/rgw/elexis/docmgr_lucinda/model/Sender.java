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

package ch.rgw.elexis.docmgr_lucinda.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.elexis.data.PersistentObject;
import ch.rgw.elexis.docmgr_lucinda.Activator;
import ch.rgw.lucinda.Handler;

/**
 * The Sender takes a List of PersistentObjects from its creating customer and sends them asynchroneously to Lucinda. 
 * Each Object gets "specified" by the customer, i.e. the customer transforms the PersistentObject into a map of properties to
 * persist.
 * 
 * @author gerry
 *
 */
public class Sender implements Handler {
	private List<? extends PersistentObject> toDo;
	private Customer customer;
	private List<Document> answers = new ArrayList<Document>();
	private List<String> onTheWay = new ArrayList<String>();

	public Sender(Customer customer, List<? extends PersistentObject> list) {
		toDo = list;
		this.customer = customer;
		sendNext();
	}

	/**
	 * Lucinda will signal each transfer of a Document. We use these signals to keep track of how many Documents are still on the way.
	 * If no more Documents are waiting and no more Documents are on the way, then we tell our customer, that we finished the job.
	 */
	@Override
	public void signal(Map<String, Object> message) {
		answers.add(new Document(message));
		onTheWay.remove(message.get("_id")); //$NON-NLS-1$
		if (toDo.isEmpty() && onTheWay.isEmpty()) {
			customer.finished(answers);
		}
		sendNext();
	}

	/*
	 * Send the next Document to Lucinda. We don't check for duplicates here, since Lucinda won't add duplicates to the index.
	 * (It will update existing documents instead)
	 */
	private void sendNext() {
		if (!toDo.isEmpty()) {
			PersistentObject po = toDo.remove(0);
			if (po.exists()) {
				Document order = customer.specify(po);
				if (order == null) {
					toDo.clear();
				} else {
					byte[] contents = (byte[]) order.toMap().get("payload"); //$NON-NLS-1$
					onTheWay.add(po.getId());
					Activator.getDefault().getLucinda().addToIndex(po.getId(), order.get("title"), //$NON-NLS-1$
							order.get("type"), order.toMap(), contents, this); //$NON-NLS-1$
				}
			}
		}
	}
}

interface Customer {
	/**
	 * Transcode a PersistentObject in a Document reflecting the customer's domain. 
	 * @param po A PersistentObject, which is of the Customer's Subtype
	 * @return A 'Document' containing at least the properties "title" (String), "type" (String), and "payload" (byte[]),
	 * and an arbitrary number of arbitrary additional properties. Recommended are: "lastname", "firstname", "birthdate" of
	 * the Patient this Document belongs to.
	 */
	public Document specify(PersistentObject po);

	/**
	 * The Sender has finished the job. Do any cleanup
	 * @param messages All messages received from Lucinda while transmitting
	 */
	public void finished(List<Document> messages);
}
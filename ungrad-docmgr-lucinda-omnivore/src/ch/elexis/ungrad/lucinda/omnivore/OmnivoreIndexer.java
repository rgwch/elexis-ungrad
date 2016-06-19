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

package ch.elexis.ungrad.lucinda.omnivore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.omnivore.data.DocHandle;
import ch.elexis.ungrad.lucinda.Activator;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.IProgressController;
import ch.elexis.ungrad.lucinda.model.Customer;
import ch.elexis.ungrad.lucinda.model.Document;
import ch.elexis.ungrad.lucinda.model.Sender;
import ch.rgw.tools.TimeTool;

public class OmnivoreIndexer implements Customer {
	Logger log = LoggerFactory.getLogger(OmnivoreIndexer.class);
	private boolean cont = false;
	private IProgressController pc;
	Long progressHandle;
	private long lastCheck;

	/**
	 * If active, the Index will run over all Consultations. If bActive==false,
	 * Indexing will stop after the next Document.
	 */
	public void setActive(boolean bActive) {
		cont = bActive;
	}

	/**
	 * Start indexing. All consultations since last run are fetched from the
	 * database. A progress indicator and a Sender are initialized
	 * 
	 * @See Sender
	 */

	public void start(IProgressController pc) {
		this.pc = pc;
		try {
			lastCheck = Long.parseLong(Preferences.get(Preferences.LASTSCAN_OMNI, "0")); //$NON-NLS-1$
		} catch (NumberFormatException nf) {
			lastCheck = 0L;
		}
		StringBuilder querySQL = new StringBuilder("SELECT ID FROM ").append(DocHandle.TABLENAME)
				.append(" WHERE lastupdate >=").append(lastCheck).append(" AND deleted='0' ORDER BY ")
				.append("lastupdate");

		Query<DocHandle> qbe = new Query<DocHandle>(DocHandle.class);
		Collection<DocHandle> docs = qbe.queryExpression(querySQL.toString(), new LinkedList<DocHandle>());

		progressHandle = pc.initProgress(docs.size());
		setActive(true);
		new Sender(this, (List<? extends PersistentObject>) docs);
	}

	/**
	 * for each hit in the List, the Sender asks its Customer to fill in values
	 * to store in the index. Mandatory fields are as follows:
	 * <ul>
	 * <li>title: A short text describing the entry. Will show up in search
	 * results</li>
	 * <li>type: A description (one word) of the type of this entry. Well also
	 * show up in the results</li>
	 * <li>payload</li> The the text to index, as byte array.</li>
	 * </ul>
	 * 
	 * The following fields are not required, but recommended:
	 * <ul>
	 * <li>lastname: Last name of the concerned patient.</li>
	 * <li>firstname: First name of the patient.</li>
	 * <li>birthdate: Date in the form yyyyMMdd</li>
	 * <li>concern: a standardized description of the patient:
	 * lastname_firstname_birthdate. this will be used to filter entries of the
	 * currently selected patient in searches.</li>
	 * </ul>
	 * 
	 * Other fields are optional.
	 * 
	 * @Returns a Map with the metadata, or null to indicate,that the sender
	 *          should finish and discard remaining objects.
	 */

	@Override
	public Document specify(PersistentObject po) {
		DocHandle dh = (DocHandle) po;
		if (cont) {
			Document meta = new Document();
			Patient patient = dh.getPatient();
			String bdRaw = get(patient, Patient.FLD_DOB);
			String lastname = get(patient, Patient.FLD_NAME);
			String firstname = get(patient, Patient.FLD_FIRSTNAME);
			String birthdate = new TimeTool(bdRaw).toString(TimeTool.DATE_COMPACT);
			String docdate = new TimeTool(dh.getCreationDate()).toString(TimeTool.DATE_COMPACT);
			if (dh.getLastUpdate() > lastCheck) {
				lastCheck = dh.getLastUpdate();
				Preferences.set(Preferences.LASTSCAN_OMNI, Long.toString(lastCheck));
			}
			StringBuilder concern = new StringBuilder().append(lastname).append("_").append(firstname).append("_") //$NON-NLS-1$ //$NON-NLS-2$
					.append(birthdate);

			meta.put("lastname", lastname); //$NON-NLS-1$
			meta.put("firstname", firstname); //$NON-NLS-1$
			meta.put("birthdate", birthdate); //$NON-NLS-1$
			meta.put("date", docdate); //$NON-NLS-1$
			meta.put("category", dh.getCategory()); //$NON-NLS-1$
			meta.put("keywords", dh.getKeywords()); //$NON-NLS-1$
			meta.put("concern", concern.toString()); //$NON-NLS-1$
			meta.put("payload", dh.getContents()); //$NON-NLS-1$
			meta.put("concern", concern.toString()); //$NON-NLS-1$
			meta.put("title", dh.getTitle()); //$NON-NLS-1$
			meta.put("type", Preferences.OMNIVORE_NAME); //$NON-NLS-1$
			pc.addProgress(progressHandle, 1);
			return meta;
		} else {
			pc.addProgress(progressHandle, Integer.MAX_VALUE);
			return null;
		}

	}

	private String get(PersistentObject po, String field) {
		String ret = po.get(field);
		if (ret == null) {
			return ""; //$NON-NLS-1$
		}
		return ret;
	}

	/**
	 * When all elements are processed, or after the customer answered "null" to
	 * the call to specify, the Sender calls finished for cleanup. Here we note
	 * the date of the last document indexed to continue later.
	 *
	 * @param messages
	 *            Lucinda messages sent while transferring.
	 */

	@Override
	public void finished(List<Document> messages) {
		Activator.getDefault().addMessages(messages);
		Preferences.cfg.flush();
	}

}

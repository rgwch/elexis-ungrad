/*******************************************************************************
 * Copyright (c) 2016-2024 by G. Weirich
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

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.omnivore.model.IDocumentHandle;
import ch.elexis.ungrad.lucinda.Activator;
import ch.elexis.ungrad.lucinda.Preferences;
import ch.elexis.ungrad.lucinda.controller.IProgressController;
import ch.elexis.ungrad.lucinda.model.Customer;
import ch.elexis.ungrad.lucinda.model.Sender;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.TimeTool;

/**
 * A Lucinda Indexer Customer for Omnivore Documents Since the DocHandle doesn't
 * seem to exist anymore, we have to access the database directly
 * 
 * @author gerry
 *
 */
public class OmnivoreIndexer implements Customer {
	Logger log = LoggerFactory.getLogger(OmnivoreIndexer.class);
	private boolean cont = false;
	private boolean bMove = false;
	private IProgressController pc;
	Long progressHandle;
	private long lastCheck;

	/**
	 * If active, the Index will run over all Consultations. If bActive==false,
	 * indexing will stop after the next Document.
	 */
	public void setActive(boolean bActive) {
		cont = bActive;
	}

	/**
	 * Start indexing. All consultations since last run are fetched from the
	 * database. A progress indicator and a Sender are initialized We use the
	 * lastupdate field to check where we ware, which is probably not accurate with
	 * older databases where that field didn't exist yet.
	 * 
	 * @throws IOException
	 * @See Sender
	 */

	public void start(IProgressController pc) {
		this.pc = pc;
		try {
			lastCheck = Long.parseLong(Preferences.get(Preferences.LASTSCAN_OMNI, "0")); //$NON-NLS-1$
		} catch (NumberFormatException nf) {
			lastCheck = 0L;
		}
		bMove = Preferences.is(Preferences.OMNIVORE_MOVE);
		StringBuilder querySQL = new StringBuilder("SELECT ID FROM CH_ELEXIS_OMNIVORE_DATA")
				.append(" WHERE lastupdate >=").append(lastCheck);
		String excludes = Preferences.get(Preferences.OMNIVORE_EXCLUDE, "");
		if (excludes.length() > 0) {
			String[] ex = excludes.split(",");
			for (String e : ex) {
				querySQL.append(" AND Category <>").append(JdbcLink.wrap(e));
			}
		}
		querySQL.append(" AND deleted='0' ORDER BY ").append("lastupdate");

		Query<PersistentObject> qbe = new Query<PersistentObject>(PersistentObject.class);
		Collection<PersistentObject> docs = qbe.queryExpression(querySQL.toString(),
				new LinkedList<PersistentObject>());

		progressHandle = pc.initProgress(docs.size());
		setActive(true);
		new Sender(this, (List<? extends PersistentObject>) docs, bMove);
	}

	/**
	 * for each hit in the List, the Sender asks its Customer to fill in values to
	 * store in the index. Mandatory fields are as follows:
	 * <ul>
	 * <li>title: A short text describing the entry. Will show up in search
	 * results</li>
	 * <li>type: A description (one word) of the type of this entry. Well also show
	 * up in the results</li>
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
	 * @Returns JsonObject with the metadata, or null to indicate,that the sender
	 *          should finish and discard remaining objects.
	 */

	@Override
	public Map specify(PersistentObject po) {
		if (cont) {
			Map<String, Object> meta = new HashMap<String, Object>();
			try {
				meta.put("payload", getContents(po)); //$NON-NLS-1$ $
			} catch (Exception ex) {
				// This version can only process database-stored Omnivore-Documents
				ExHandler.handle(ex);
				return null;
			}

			Patient patient = Patient.load(po.get("PatID"));
			String bdRaw = get(patient, Patient.FLD_DOB);
			String lastname = get(patient, Patient.FLD_NAME);
			String firstname = get(patient, Patient.FLD_FIRSTNAME);
			String birthdate = new TimeTool(bdRaw).toString(TimeTool.DATE_GER);
			String docdate = new TimeTool(po.get("CreationDate")).toString(TimeTool.DATE_COMPACT);
			if (po.getLastUpdate() > lastCheck) {
				lastCheck = po.getLastUpdate();
				Preferences.set(Preferences.LASTSCAN_OMNI, Long.toString(lastCheck));
			}
			StringBuilder concern = new StringBuilder().append(lastname).append("_").append(firstname).append("_") //$NON-NLS-1$ //$NON-NLS-2$
					.append(birthdate);

			meta.put("lastname", lastname); //$NON-NLS-1$
			meta.put("firstname", firstname); //$NON-NLS-1$
			meta.put("birthdate", birthdate); //$NON-NLS-1$
			meta.put("date", docdate); //$NON-NLS-1$
			meta.put("category", po.get("Category")); //$NON-NLS-1$
			meta.put("keywords", po.get("Keywords")); //$NON-NLS-1$
			meta.put("concern", concern.toString()); //$NON-NLS-1$
			meta.put("title", po.get("title")); //$NON-NLS-1$
			meta.put("type", Preferences.OMNIVORE_NAME); //$NON-NLS-1$
			meta.put("filetype", po.get("Mimetype"));
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
	 * When all elements are processed, or after the customer answered "null" to the
	 * call to specify, the Sender calls finished for cleanup. Here we note the date
	 * of the last document indexed to continue later.
	 *
	 * @param messages Lucinda messages sent while transferring.
	 */

	@Override
	public void finished(List<Map<String, Object>> messages) {
		Activator.getDefault().addMessages(messages);
		Preferences.cfg.flush();
	}

	/**
	 * A Document was indexed successfully. if bMove: remove document from omnivore
	 * from Omnivore.
	 * 
	 */
	@Override
	public void success(String id) {
		if (bMove) {
			PreparedStatement ps = PersistentObject.getConnection()
					.getPreparedStatement("UPDATE CH_ELEXIS_OMNIVORE_DATA SET deleted=? WHERE id=?");
			Query<PersistentObject> qbe = new Query<PersistentObject>(PersistentObject.class);
			qbe.execute(ps, new String[] { "1", id });
		}
	}

	public byte[] getContents(PersistentObject po) throws Exception {
		byte[] ret = po.getBinary("Doc");
		if (ret == null) {
			throw new Exception("cant retrieve file system based Omnivore files");
		}
		return ret;
	}
}

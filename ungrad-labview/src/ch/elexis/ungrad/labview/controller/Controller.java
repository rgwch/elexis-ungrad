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
package ch.elexis.ungrad.labview.controller;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.controller.condensed.CondensedViewController;
import ch.elexis.ungrad.labview.controller.condensed.Exporter;
import ch.elexis.ungrad.labview.controller.full.FullViewController;
import ch.elexis.ungrad.labview.controller.smart.SmartViewController;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.ungrad.labview.views.LaborView;

public class Controller {

	LaborView view;
	LabResultsSheet lrs = new LabResultsSheet();
	Logger log = LoggerFactory.getLogger("Labview Controller");
	Resolver resolver = new Resolver();
	CondensedViewController ctlCond = new CondensedViewController(this);
	FullViewController ctlFull = new FullViewController(this);
	SmartViewController ctlSmart = new SmartViewController(this);

	public Controller(LaborView view) {
		this.view = view;
	}

	public LabResultsSheet getLRS() {
		return lrs;
	}

	public Control createSummaryControl(CTabFolder ctf) {
		return ctlCond.createControl(ctf);
	}

	public Control createFullControl(CTabFolder ctf) {
		return ctlFull.createControl(ctf);
	}

	public Control createSmartControl(CTabFolder ctf) {
		return ctlSmart.createControl(ctf);
	}

	public void saveState() {
		Preferences.cfg.set(Preferences.CONDVIEW, ctlCond.getState());
	}

	public void loadState() {
		String colWidths = Preferences.cfg.get(Preferences.CONDVIEW, "150,150,100,130,130,130");
		ctlCond.setState(colWidths);

	}

	public void dispose() {
		ctlCond.dispose();
	}

	public void setPatient(Patient pat) throws ElexisException {
		lrs.setPatient(pat);
		/*
		 * lcp.setPatient(pat); colsSummary.reload(lcp);
		 * tvSummary.setInput(pat);
		 */
	}

	public Exporter getExporter() {
		return ctlCond.getExporter();
	}

	@SuppressWarnings("deprecation")
	public void purgeLabItems() {
		Job job = new Job("purge lab items") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				PersistentObject.getConnection().exec("DELETE FROM LABORWERTE WHERE deleted='1'");
				PersistentObject.getConnection().exec("DELETE FROM LABORITEMS WHERE deleted='1'");
				Query<LabItem> qbe = new Query<LabItem>(LabItem.class);
				List<LabItem> items = qbe.execute();
				monitor.beginTask("purge unusedlab items", items.size());
				for (LabItem li : items) {
					Query<LabResult> qlr = new Query<LabResult>(LabResult.class);
					qlr.add(LabResult.ITEM_ID, Query.EQUALS, li.getId());
					if (qlr.execute().isEmpty()) {
						log.info("deleting " + li.getLabel());
						li.delete();
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					monitor.worked(1);
				}
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						try {
							lrs.reload();
						} catch (ElexisException e) {
							e.printStackTrace();
							log.error("could not reload Lab Items", e);
						}

					}

				});
				return Status.OK_STATUS;

			}

		};
		job.setPriority(Job.LONG);
		job.setUser(true);
		job.schedule();
	}

}

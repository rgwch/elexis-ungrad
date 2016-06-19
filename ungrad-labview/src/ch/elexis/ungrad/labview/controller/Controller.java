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

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.controller.condensed.CondensedViewController;
import ch.elexis.ungrad.labview.controller.condensed.Exporter;
import ch.elexis.ungrad.labview.controller.full.FullViewController;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.ungrad.labview.views.LaborView;

public class Controller {
	
	LaborView view;
	LabResultsSheet lrs=new LabResultsSheet();
	Log log = Log.get("Labview Controller");
	Resolver resolver = new Resolver();
	CondensedViewController ctlCond=new CondensedViewController(this);
	FullViewController ctlFull=new FullViewController(this);
	

	public Controller(LaborView view) {
		this.view = view;
	}

	public LabResultsSheet getLRS(){
		return lrs;
	}
	
	public Control createSummaryControl(CTabFolder ctf){
		return ctlCond.createControl(ctf);
	}
	
	public Control createFullControl(CTabFolder ctf) {
		return ctlFull.createControl(ctf);
	}
	
	public void saveState() {
		Preferences.cfg.set(Preferences.CONDVIEW, ctlCond.getState());
	}

	public void loadState() {
		String colWidths = Preferences.cfg.get(Preferences.CONDVIEW, "150,150,100,130,130,130");
		ctlCond.setState(colWidths);

	}

	public void dispose(){
		ctlCond.dispose();
	}


	public void setPatient(Patient pat) throws ElexisException {
		lrs.setPatient(pat);
		/*
		lcp.setPatient(pat);
		colsSummary.reload(lcp);
		tvSummary.setInput(pat);
		*/
	}

	public Exporter getExporter(){
		return ctlCond.getExporter();
	}

	

	
}

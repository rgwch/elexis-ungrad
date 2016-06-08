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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.views.LaborView;

public class Controller {
	LabContentProvider lcp;
	LaborView view;
	TreeViewer tv;
	LabTableColumns cols;

	public Controller(LaborView view) {
		lcp = new LabContentProvider();
		this.view = view;
	}

	public void saveState(){
		StringBuilder cw=new StringBuilder();
		for(int i=0;i<cols.cols.length;i++){
			cw.append(Integer.toString(cols.cols[i].getColumn().getWidth())).append(",");
		}
		String widths=cw.substring(0,cw.length()-1);
		Preferences.cfg.set(Preferences.COLWIDTHS, widths);
	}
	
	public void loadState(){
		String colWidths=Preferences.cfg.get(Preferences.COLWIDTHS, "150,150,100,130,130,130");
		int max=cols.cols.length;
		int i=0;
		for(String w:colWidths.split(",")){
			if(i<max){
			cols.cols[i].getColumn().setWidth(Integer.parseInt(w));
			}
		}
		
	}
	public Control createPartControl(Composite parent) {
		tv = new TreeViewer(parent);
		tv.setContentProvider(lcp);
		tv.setUseHashlookup(true);
		Tree tree = tv.getTree();
		cols = new LabTableColumns(lcp.lrs, tv);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tv.setAutoExpandLevel(2);
		tv.setInput(lcp);
		loadState();
		return tree;
	}

	public void setPatient(Patient pat) throws ElexisException {
		lcp.setPatient(pat);
		cols.reload(lcp);
		tv.setInput(pat);
	}

	public void dispose() {
		saveState();
		cols.dispose();
	}

}

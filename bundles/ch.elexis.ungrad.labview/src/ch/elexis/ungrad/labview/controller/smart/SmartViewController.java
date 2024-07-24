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

package ch.elexis.ungrad.labview.controller.smart;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IPatient;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

/**
 * The Smart Lab Viewer shows a number of columns and a summury of earlier values in the rightmost
 * column
 * 
 * @author gerry
 *
 */
public class SmartViewController implements IObserver {
	TreeViewer tvSmart;
	Controller controller;
	SmartTreeColumns stc;
	Logger log = LoggerFactory.getLogger(getClass());
	
	SmartContentProvider scp;
	
	public SmartViewController(Controller parent){
		controller = parent;
		scp = new SmartContentProvider(controller.getLRS());
		LabResultsSheet lrs = controller.getLRS();
		lrs.addObserver(this);
		
	}
	
	@Override
	public void signal(Object message){
		if (message instanceof IPatient) {
			stc.saveColLayout();
			stc.reload(controller);
			tvSmart.setInput(message);
		}
		
	}
	
	public LabResultsSheet getLRS(){
		return controller.getLRS();
	}
	
	public Control createControl(Composite parent){
		tvSmart = new TreeViewer(parent);
		tvSmart.setContentProvider(scp);
		tvSmart.setUseHashlookup(true);
		Tree tree = tvSmart.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		stc = new SmartTreeColumns(tvSmart);
		tvSmart.setAutoExpandLevel(2);
		tvSmart.setInput(scp);
		return tree;
	}
	
	public void dispose(){
		controller.getLRS().removeObserver(this);
	}
	
	public String getState(){
		stc.saveColLayout();
		StringBuilder ret = new StringBuilder();
		for (int w : stc.widths) {
			ret.append(Integer.toString(w)).append(",");
		}
		return ret.substring(0, ret.length() - 1);
	}
	
	public void setState(String parms){
		if (parms != null && parms.length() > 1) {
			String[] cols = parms.split(",");
			int[] icols = new int[cols.length];
			for (int i = 0; i < cols.length; i++) {
				try {
					int size = Integer.parseInt(cols[i]);
					icols[i] = size;
				} catch (NumberFormatException nex) {
					log.error("Bad number format in saved state ", nex);
				}
			}
			stc.setColWidths(icols);
		}
	}
	
}

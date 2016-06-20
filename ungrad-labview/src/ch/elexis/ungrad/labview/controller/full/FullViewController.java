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
package ch.elexis.ungrad.labview.controller.full;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.LabResultsSheet;

public class FullViewController implements IObserver {
	TreeViewer tvFull;
	Controller controller;
	FullDisplayTreeColumns fdtc;
	
	public FullViewController(Controller parent){
		controller = parent;
	}
	
	public LabResultsSheet getLRS(){
		return controller.getLRS();
	}
	
	public Control createControl(Composite parent){
		tvFull = new TreeViewer(parent);
		Tree tree = tvFull.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvFull.setContentProvider(new FullContentProvider(controller.getLRS()));
		fdtc = new FullDisplayTreeColumns(tvFull);
		controller.getLRS().addObserver(this);
		return tree;
	}
	
	@Override
	public void signal(Object message){
		if (message instanceof Patient) {
			fdtc.reload(this);
			tvFull.setInput(message);
		}
	}
}

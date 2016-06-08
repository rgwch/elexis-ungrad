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

	public Control createPartControl(Composite parent) {
		tv = new TreeViewer(parent);
		tv.setContentProvider(lcp);
		tv.setUseHashlookup(true);
		Tree tree = tv.getTree();
		cols = new LabTableColumns(lcp.lrs, tv, 10);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tv.setInput(lcp);
		return tree;
	}

	public void setPatient(Patient pat) throws ElexisException {
		lcp.setPatient(pat);
		cols.reload(lcp);
		tv.setInput(pat);
	}

	public void dispose() {
		cols.dispose();
	}

}

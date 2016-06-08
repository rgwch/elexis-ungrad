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

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.model.LabResultsRow;
import ch.elexis.ungrad.labview.model.Result;
import ch.elexis.ungrad.labview.views.LaborView;
import ch.rgw.io.FileTool;

public class Controller {
	LabContentProvider lcp;
	LaborView view;
	TreeViewer tv;
	LabTableColumns cols;

	public Controller(LaborView view) {
		lcp = new LabContentProvider();
		this.view = view;
	}

	public void saveState() {
		StringBuilder cw = new StringBuilder();
		for (int i = 0; i < cols.cols.length; i++) {
			cw.append(Integer.toString(cols.cols[i].getColumn().getWidth())).append(",");
		}
		String widths = cw.substring(0, cw.length() - 1);
		Preferences.cfg.set(Preferences.COLWIDTHS, widths);
	}

	public void loadState() {
		String colWidths = Preferences.cfg.get(Preferences.COLWIDTHS, "150,150,100,130,130,130");
		int max = cols.cols.length;
		int i = 0;
		for (String w : colWidths.split(",")) {
			if (i < max) {
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
		cols.dispose();
	}

	public boolean createHTML(Composite parent) {
		FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
		String file = fd.open();
		if (file != null) {

			StringBuilder html = new StringBuilder("<html><head></head><body><table>");

			for (Object o : lcp.getElements(this)) {
				html.append("<tr><th>").append(o.toString()).append("</th></tr>");
				for (Object l : lcp.getChildren(o)) {
					if (l instanceof LabResultsRow) {
						LabResultsRow lr=(LabResultsRow)l;
						html.append("<tr>");
						html.append("<td><em>").append(lr.getItem().get("titel")).append("</em></td>");
						for(Result res:lr.getResults()){
							html.append("<td>").append(res.get("resultat")).append("</td>");
						}
						html.append("</tr>");
					}
				}
			}
			html.append("</table></body></html>");
			try {
				FileTool.writeTextFile(new File(file), html.toString());
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}
}

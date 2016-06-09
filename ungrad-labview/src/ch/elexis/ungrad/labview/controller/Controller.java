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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.Patient;
import ch.elexis.ungrad.Resolver;
import ch.elexis.ungrad.labview.Preferences;
import ch.elexis.ungrad.labview.views.LaborView;

public class Controller {
	LabSummaryContentProvider lcp;
	LaborView view;
	TreeViewer tvSummary;
	TableViewer tvInput;
	LabSummaryTableColumns cols;
	Log log = Log.get("Labview Controller");
	Resolver resolver = new Resolver();

	public Controller(LaborView view) {
		lcp = new LabSummaryContentProvider();
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

	public Control createSummaryControl(Composite parent) {
		tvSummary = new TreeViewer(parent);
		tvSummary.setContentProvider(lcp);
		tvSummary.setUseHashlookup(true);
		Tree tree = tvSummary.getTree();
		cols = new LabSummaryTableColumns(lcp.lrs, tvSummary);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvSummary.setAutoExpandLevel(2);
		tvSummary.setInput(lcp);
		loadState();
		return tree;
	}

	public Control createInputControl(CTabFolder parent) {
		tvInput = new TableViewer(parent);
		Table table = tvInput.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		TableViewerColumn tvcTitle = new TableViewerColumn(tvInput, SWT.LEFT);
		TableViewerColumn tvcValues = new TableViewerColumn(tvInput, SWT.NONE);
		tvcTitle.getColumn().setText("Parameter");
		tvcTitle.getColumn().setWidth(100);
		tvcValues.setEditingSupport(new LabInputEditingSupport(tvInput));
		tvInput.setContentProvider(new LabInputContentProvider(lcp.lrs));
		tvInput.setLabelProvider(new LabInputLabelProvider());
		parent.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				CTabItem it = (CTabItem) e.item;

			}

		});
		return table;
	}

	public void setPatient(Patient pat) throws ElexisException {
		lcp.setPatient(pat);
		cols.reload(lcp);
		tvSummary.setInput(pat);
	}

	public void dispose() {
		cols.dispose();
	}

	public Exporter getExporter() {
		return new Exporter(lcp);
	}
}

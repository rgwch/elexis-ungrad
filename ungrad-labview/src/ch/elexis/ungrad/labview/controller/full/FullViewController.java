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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import ch.elexis.data.Patient;
import ch.elexis.ungrad.IObserver;
import ch.elexis.ungrad.labview.controller.Controller;
import ch.elexis.ungrad.labview.model.Item;
import ch.elexis.ungrad.labview.model.LabResultsSheet;
import ch.elexis.ungrad.labview.model.Result;
import ch.rgw.tools.TimeTool;

public class FullViewController implements IObserver {
	TreeViewer tvFull;
	Controller controller;
	FullDisplayTreeColumns fdtc;
	TreeViewerFocusCellManager focusManager;
	TextCellEditor tce;

	public FullViewController(Controller parent) {
		controller = parent;
	}

	public LabResultsSheet getLRS() {
		return controller.getLRS();
	}

	public Control createControl(Composite parent) {
		tvFull = new TreeViewer(parent);
		Tree tree = tvFull.getTree();
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tvFull.setContentProvider(new FullContentProvider(controller.getLRS()));
		fdtc = new FullDisplayTreeColumns(this);
		tce = new TextCellEditor(tree);
		focusManager = new TreeViewerFocusCellManager(tvFull, new FocusCellHighlighter(tvFull) {
		});

		controller.getLRS().addObserver(this);
		return tree;
	}

	@Override
	public void signal(Object message) {
		if (message instanceof Patient) {
			fdtc.reload();
			tvFull.setInput(message);
		}
	}

	EditingSupport createEditingSupportFor(TreeViewerColumn tvc, TimeTool colDate) {
		return new EditingSupport(tvFull) {

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof Item) {
					Result result = getLRS().getResultForDate((Item) element, colDate);
					if(result==null){
						result=new Result(0f);
					}
					result.set("resultat", (String) value);
					result.set("ItemID", ((Item)element).get("ID"));
					result.set("Datum", colDate.toString(TimeTool.DATE_COMPACT));
					result.set("Zeit", colDate.toString(TimeTool.TIME_SMALL));
					getLRS().addResult(result);
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof Item) {
					Result result = getLRS().getResultForDate((Item) element, colDate);
					if (result == null) {
						result = new Result(0f);
					}
					return result.get("resultat");
				} else {
					return "";
				}
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return tce;
			}

			@Override
			protected boolean canEdit(Object element) {
				return element instanceof Item;
			}
		};
	}
}
